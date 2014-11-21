package de.dentrassi.pm.storage.service.internal;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import com.google.common.io.ByteStreams;

import de.dentrassi.pm.common.service.AbstractJpaServiceImpl;
import de.dentrassi.pm.storage.jpa.ArtifactEntity;
import de.dentrassi.pm.storage.jpa.ChannelEntity;
import de.dentrassi.pm.storage.service.Artifact;
import de.dentrassi.pm.storage.service.ArtifactInformation;
import de.dentrassi.pm.storage.service.ArtifactReceiver;
import de.dentrassi.pm.storage.service.Channel;
import de.dentrassi.pm.storage.service.StorageService;

public class StorageServiceImpl extends AbstractJpaServiceImpl implements StorageService
{
    @Override
    public Channel createChannel ()
    {
        final ChannelEntity channel = new ChannelEntity ();
        return doWithTransaction ( em -> {
            em.persist ( channel );
            return convert ( channel );
        } );
    }

    @Override
    public void deleteChannel ( final String channelId )
    {
        doWithTransactionVoid ( em -> em.remove ( em.getReference ( ChannelEntity.class, channelId ) ) );
    }

    @Override
    public Channel getChannel ( final String channelId )
    {
        return doWithTransaction ( em -> {
            final ChannelEntity channel = em.find ( ChannelEntity.class, channelId );
            return convert ( channel );
        } );
    }

    @Override
    public Artifact createArtifact ( final String channelId, final String name, final InputStream stream )
    {
        try
        {
            final ArtifactEntity artifact = new ArtifactEntity ();
            artifact.setName ( name );

            return doWithTransaction ( em -> {

                final ChannelEntity channel = em.find ( ChannelEntity.class, channelId );

                if ( channel == null )
                {
                    throw new IllegalArgumentException ( String.format ( "Channel %s unknown", channelId ) );
                }

                artifact.setChannel ( channel );

                // set the blob

                final Connection c = em.unwrap ( Connection.class );

                long size;

                final Blob blob = c.createBlob ();
                try
                {
                    try ( OutputStream s = blob.setBinaryStream ( 1 ) )
                    {
                        size = ByteStreams.copy ( stream, s );
                    }

                    artifact.setSize ( size );
                    em.persist ( artifact );
                    em.flush ();

                    try ( PreparedStatement ps = c.prepareStatement ( "update ARTIFACTS set data=?" ) )
                    {
                        ps.setBlob ( 1, blob );
                        ps.executeUpdate ();
                    }
                }
                finally
                {
                    blob.free ();
                }

                return new ArtifactImpl ( new ChannelImpl ( channelId, StorageServiceImpl.this ), artifact.getId (), name, size );

            } );
        }
        finally
        {
            try
            {
                stream.close ();
            }
            catch ( final IOException e )
            {
                throw new RuntimeException ( e );
            }
        }
    }

    public Set<Artifact> listArtifacts ( final String channelId )
    {
        return doWithTransaction ( em -> {

            final ChannelEntity ce = em.find ( ChannelEntity.class, channelId );

            if ( ce == null )
            {
                throw new IllegalArgumentException ( String.format ( "Channel %s not found", channelId ) );
            }

            final CriteriaBuilder cb = em.getCriteriaBuilder ();
            final CriteriaQuery<ArtifactEntity> cq = cb.createQuery ( ArtifactEntity.class );
            final Root<ArtifactEntity> root = cq.from ( ArtifactEntity.class );
            cq.select ( root );

            final TypedQuery<ArtifactEntity> q = em.createQuery ( cq );

            final ChannelImpl channel = convert ( ce );
            final List<ArtifactEntity> rl = q.getResultList ();

            final Set<Artifact> result = new TreeSet<> ();
            for ( final ArtifactEntity ae : rl )
            {
                result.add ( convert ( channel, ae ) );
            }

            return result;
        } );
    }

    private ChannelImpl convert ( final ChannelEntity ce )
    {
        if ( ce == null )
        {
            return null;
        }
        return new ChannelImpl ( ce.getId (), this );
    }

    private Artifact convert ( final ChannelImpl channel, final ArtifactEntity ae )
    {
        if ( ae == null )
        {
            return null;
        }
        return new ArtifactImpl ( channel, ae.getId (), ae.getName (), ae.getSize () );
    }

    @Override
    public void streamArtifact ( final String artifactId, final ArtifactReceiver receiver )
    {
        doWithTransactionVoid ( em -> {

            final ArtifactEntity ae = em.find ( ArtifactEntity.class, artifactId );

            if ( ae == null )
            {
                throw new IllegalArgumentException ( String.format ( "Artifact %s not found", artifactId ) );
            }

            final Connection c = em.unwrap ( Connection.class );
            try ( PreparedStatement ps = c.prepareStatement ( "select DATA from ARTIFACTS where ID=?" ) )
            {
                ps.setObject ( 1, artifactId );
                try ( ResultSet rs = ps.executeQuery () )
                {
                    if ( !rs.next () )
                    {
                        throw new FileNotFoundException ();
                    }

                    final Blob blob = rs.getBlob ( 1 );
                    try ( InputStream stream = blob.getBinaryStream () )
                    {
                        receiver.receive ( new ArtifactInformation ( ae.getSize (), ae.getName () ), stream );
                    }
                    finally
                    {
                        blob.free ();
                    }
                }
            }

        } );
    }

    @Override
    public Collection<Channel> listChannels ()
    {
        return doWithTransaction ( em -> {
            final CriteriaQuery<ChannelEntity> cq = em.getCriteriaBuilder ().createQuery ( ChannelEntity.class );

            final TypedQuery<ChannelEntity> q = em.createQuery ( cq );
            final List<ChannelEntity> rl = q.getResultList ();

            final List<Channel> result = new ArrayList<> ( rl.size () );
            for ( final ChannelEntity ce : rl )
            {
                result.add ( convert ( ce ) );
            }

            return result;
        } );
    }
}
