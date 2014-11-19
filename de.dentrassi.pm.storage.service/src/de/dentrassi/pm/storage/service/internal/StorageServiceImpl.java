package de.dentrassi.pm.storage.service.internal;

import java.io.InputStream;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import de.dentrassi.pm.common.service.AbstractJpaServiceImpl;
import de.dentrassi.pm.storage.jpa.ArtifactEntity;
import de.dentrassi.pm.storage.jpa.ArtifactEntity_;
import de.dentrassi.pm.storage.jpa.ChannelEntity;
import de.dentrassi.pm.storage.service.Artifact;
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
    public Artifact createArtifact ( final String channelId, final InputStream stream )
    {
        final ArtifactEntity artifact = new ArtifactEntity ();

        doWithTransactionVoid ( em -> {

            final ChannelEntity channel = em.find ( ChannelEntity.class, channelId );

            if ( channel == null )
            {
                throw new IllegalArgumentException ( String.format ( "Channel %s unknown", channelId ) );
            }

            artifact.setChannel ( channel );

            em.persist ( artifact );
        } );

        return new ArtifactImpl ( new ChannelImpl ( channelId, this ), artifact.getId () );
    }

    public Set<Artifact> listArtifacts ( final String channelId )
    {
        return doWithTransaction ( em -> {

            final ChannelEntity ce = em.find ( ChannelEntity.class, channelId );

            if ( ce == null )
            {
                throw new IllegalArgumentException ( String.format ( "Channel %s not found", channelId ) );
            }

            final ChannelImpl channel = convert ( ce );

            final CriteriaBuilder builder = em.getCriteriaBuilder ();
            final CriteriaQuery<ArtifactEntity> cq = builder.createQuery ( ArtifactEntity.class );

            final Root<ArtifactEntity> root = cq.from ( ArtifactEntity.class );
            cq.where ( builder.equal ( root.get ( ArtifactEntity_.channel ), ce ) );

            final TypedQuery<ArtifactEntity> q = em.createQuery ( cq );

            final List<ArtifactEntity> rl = q.getResultList ();

            final Set<Artifact> result = new HashSet<> ( rl.size () );
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
        return new ArtifactImpl ( channel, ae.getId () );
    }

    public void streamData ( final String artifactId, final Consumer<InputStream> consumer )
    {
        doWithTransactionVoid ( em -> {

            final ArtifactEntity ae = em.find ( ArtifactEntity.class, artifactId );

            if ( ae == null )
            {
                throw new IllegalArgumentException ( String.format ( "Artifact %s not found", artifactId ) );
            }

            final Blob data = ae.getData ();
            try ( InputStream stream = data.getBinaryStream () )
            {
                consumer.accept ( stream );
            }
            finally
            {
                data.free ();
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
