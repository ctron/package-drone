/*******************************************************************************
 * Copyright (c) 2014 Jens Reimann.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jens Reimann - initial API and implementation
 *******************************************************************************/
package de.dentrassi.pm.storage.service.internal;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import com.google.common.io.ByteStreams;

import de.dentrassi.pm.aspect.ChannelAspect;
import de.dentrassi.pm.aspect.ChannelAspectInformation;
import de.dentrassi.pm.aspect.ChannelAspectProcessor;
import de.dentrassi.pm.common.service.AbstractJpaServiceImpl;
import de.dentrassi.pm.meta.extract.Extractor;
import de.dentrassi.pm.storage.jpa.ArtifactEntity;
import de.dentrassi.pm.storage.jpa.ArtifactPropertyEntity;
import de.dentrassi.pm.storage.jpa.ChannelEntity;
import de.dentrassi.pm.storage.service.Artifact;
import de.dentrassi.pm.storage.service.ArtifactInformation;
import de.dentrassi.pm.storage.service.ArtifactReceiver;
import de.dentrassi.pm.storage.service.Channel;
import de.dentrassi.pm.storage.service.MetaKey;
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
            final Path file = Files.createTempFile ( "blob-", null );

            // copy data to temp file
            try ( BufferedOutputStream os = new BufferedOutputStream ( new FileOutputStream ( file.toFile () ) ) )
            {
                ByteStreams.copy ( stream, os );
            }

            try
            {
                return doWithTransaction ( em -> {

                    final Map<MetaKey, String> metadata = extractMetaData ( em, channelId, file );

                    try ( BufferedInputStream in = new BufferedInputStream ( new FileInputStream ( file.toFile () ) ) )
                    {
                        return storeBlob ( em, channelId, name, in, metadata );
                    }
                } );
            }
            finally
            {
                try
                {
                    // delete the temp file, if possible
                    Files.deleteIfExists ( file );
                }
                catch ( final Exception e )
                {
                    // ignore this
                }
            }
        }
        catch ( final Exception e )
        {
            throw new RuntimeException ( e );
        }
        finally
        {
            // always close the stream we got
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

    protected Map<MetaKey, String> extractMetaData ( final EntityManager em, final String channelId, final Path file )
    {
        final ChannelEntity channel = getCheckedChannel ( em, channelId );

        final Map<MetaKey, String> metadata = new HashMap<> ();

        Activator.getChannelAspects ().process ( channel.getAspects (), ChannelAspect::getExtractor, extractor -> {
            try
            {
                final Map<String, String> md = new HashMap<> ();
                extractor.extractMetaData ( file, md );

                convertMetaDataFromExtractor ( metadata, extractor, md );
            }
            catch ( final Exception e )
            {
                throw new RuntimeException ( e );
            }
        } );

        return metadata;
    }

    private void convertMetaDataFromExtractor ( final Map<MetaKey, String> metadata, final Extractor extractor, final Map<String, String> md )
    {
        final String ns = extractor.getAspect ().getId ();
        for ( final Map.Entry<String, String> mde : md.entrySet () )
        {
            metadata.put ( new MetaKey ( ns, mde.getKey () ), mde.getValue () );
        }
    }

    protected ChannelEntity getCheckedChannel ( final EntityManager em, final String channelId )
    {
        final ChannelEntity channel = em.find ( ChannelEntity.class, channelId );
        if ( channel == null )
        {
            throw new IllegalArgumentException ( String.format ( "Channel %s unknown", channelId ) );
        }
        return channel;
    }

    protected ArtifactImpl storeBlob ( final EntityManager em, final String channelId, final String name, final InputStream stream, final Map<MetaKey, String> metadata ) throws SQLException, IOException
    {
        final ArtifactEntity artifact = new ArtifactEntity ();
        artifact.setName ( name );

        final ChannelEntity channel = getCheckedChannel ( em, channelId );
        artifact.setChannel ( channel );

        final Collection<ArtifactPropertyEntity> props = artifact.getProperties ();
        convertProperties ( metadata, artifact, props );

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

            // we can only set it now, since we only have the size
            artifact.setSize ( size );
            em.persist ( artifact );
            em.flush ();

            try ( PreparedStatement ps = c.prepareStatement ( "update ARTIFACTS set data=? where id=?" ) )
            {
                ps.setBlob ( 1, blob );
                ps.setString ( 2, artifact.getId () );
                ps.executeUpdate ();
            }
        }
        finally
        {
            blob.free ();
        }

        return new ArtifactImpl ( new ChannelImpl ( channelId, StorageServiceImpl.this ), artifact.getId (), name, size, metadata );
    }

    private void convertProperties ( final Map<MetaKey, String> metadata, final ArtifactEntity artifact, final Collection<ArtifactPropertyEntity> props )
    {
        for ( final Map.Entry<MetaKey, String> entry : metadata.entrySet () )
        {
            final ArtifactPropertyEntity ap = new ArtifactPropertyEntity ();

            ap.setArtifact ( artifact );
            ap.setKey ( entry.getKey ().getKey () );
            ap.setNamespace ( entry.getKey ().getNamespace () );
            ap.setValue ( entry.getValue () );

            props.add ( ap );
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

        final Map<MetaKey, String> metadata = new HashMap<> ();
        for ( final ArtifactPropertyEntity entry : ae.getProperties () )
        {
            metadata.put ( new MetaKey ( entry.getNamespace (), entry.getKey () ), entry.getValue () );
        }

        return new ArtifactImpl ( channel, ae.getId (), ae.getName (), ae.getSize (), metadata );
    }

    @Override
    public void streamArtifact ( final String artifactId, final ArtifactReceiver receiver )
    {
        doWithTransactionVoid ( em -> {
            final ArtifactEntity ae = getCheckedArtifact ( em, artifactId );
            internalStreamArtifact ( em, ae, receiver );
        } );
    }

    private ArtifactEntity getCheckedArtifact ( final EntityManager em, final String artifactId )
    {
        final ArtifactEntity ae = em.find ( ArtifactEntity.class, artifactId );

        if ( ae == null )
        {
            throw new IllegalArgumentException ( String.format ( "Artifact %s not found", artifactId ) );
        }
        return ae;
    }

    private void internalStreamArtifact ( final EntityManager em, final ArtifactEntity ae, final ArtifactReceiver receiver ) throws Exception
    {
        final String channelId = ae.getChannel ().getId ();
        final String artifactId = ae.getId ();

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
                    receiver.receive ( new ArtifactInformation ( ae.getSize (), ae.getName (), channelId ), stream );
                }
                finally
                {
                    blob.free ();
                }
            }
        }
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

    @Override
    public ArtifactInformation deleteArtifact ( final String artifactId )
    {
        return doWithTransaction ( em -> {

            final ArtifactEntity ae = em.find ( ArtifactEntity.class, artifactId );
            if ( ae == null )
            {
                return null; // silently ignore
            }

            final ArtifactInformation info = convert ( ae );

            em.remove ( ae );

            return info;
        } );
    }

    private ArtifactInformation convert ( final ArtifactEntity ae )
    {
        return new ArtifactInformation ( ae.getSize (), ae.getName (), ae.getChannel ().getId () );
    }

    public List<ChannelAspectInformation> getChannelAspectInformations ( final String channelId )
    {
        return doWithTransaction ( em -> {
            final ChannelEntity channel = getCheckedChannel ( em, channelId );
            return Activator.getChannelAspects ().resolve ( channel.getAspects () );
        } );
    }

    @Override
    public void addChannelAspect ( final String channelId, final String aspectFactoryId )
    {
        doWithTransactionVoid ( em -> {
            final ChannelEntity channel = getCheckedChannel ( em, channelId );
            channel.getAspects ().add ( aspectFactoryId );
            em.persist ( channel );

            reprocessAspect ( em, channel, aspectFactoryId );
        } );
    }

    protected void reprocessAspect ( final EntityManager em, final ChannelEntity channel, final String aspectFactoryId ) throws Exception
    {
        for ( final ArtifactEntity ae : channel.getArtifacts () )
        {
            internalStreamArtifact ( em, ae, ( info, stream ) -> {
                final Path file = Files.createTempFile ( "blob-", "-reproc" );
                try
                {
                    // stream blob to temp file

                    try ( BufferedOutputStream os = new BufferedOutputStream ( new FileOutputStream ( file.toFile () ) ) )
                    {
                        ByteStreams.copy ( stream, os );
                    }

                    // generate metadata for new factory

                    final Map<MetaKey, String> metadata = new HashMap<> ();
                    final ChannelAspectProcessor ca = Activator.getChannelAspects ();
                    ca.process ( Arrays.asList ( aspectFactoryId ), ChannelAspect::getExtractor, extractor -> {
                        try
                        {
                            final Map<String, String> md = new HashMap<> ();
                            extractor.extractMetaData ( file, md );
                            convertMetaDataFromExtractor ( metadata, extractor, md );
                        }
                        catch ( final Exception e )
                        {
                            throw new RuntimeException ( e );
                        }
                    } );

                    // add metadata

                    convertProperties ( metadata, ae, ae.getProperties () );
                    em.persist ( ae );
                }
                finally
                {
                    try
                    {
                        // delete temp file, if possible
                        Files.deleteIfExists ( file );
                    }
                    catch ( final Exception e )
                    {
                        // ignore
                    }
                }
            } );
        }
    }

    @Override
    public void removeChannelAspect ( final String channelId, final String aspectFactoryId )
    {
        doWithTransactionVoid ( em -> {
            final ChannelEntity channel = getCheckedChannel ( em, channelId );
            channel.getAspects ().remove ( aspectFactoryId );
            em.persist ( channel );

            final Query q = em.createQuery ( String.format ( "DELETE from %s ap where ap.namespace=:factoryId and ap.artifact.channel.id=:channelId", ArtifactPropertyEntity.class.getSimpleName () ) );
            q.setParameter ( "factoryId", aspectFactoryId );
            q.setParameter ( "channelId", channelId );
            q.executeUpdate ();
        } );
    }

    @Override
    public ArtifactInformation getArtifactInformation ( final String artifactId )
    {
        return doWithTransaction ( em -> convert ( getCheckedArtifact ( em, artifactId ) ) );
    }

    @Override
    public Artifact getArtifact ( final String artifactId )
    {
        return doWithTransaction ( em -> {
            final ArtifactEntity artifact = getCheckedArtifact ( em, artifactId );
            final ChannelImpl channel = convert ( artifact.getChannel () );
            return convert ( channel, artifact );
        } );
    }

    public Map<MetaKey, String> applyMetaData ( final String artifactId, final Map<MetaKey, String> metadata )
    {
        return doWithTransaction ( em -> {
            final ArtifactEntity artifact = getCheckedArtifact ( em, artifactId );
            final Map<MetaKey, String> result = convert ( artifact.getProperties () );

            // apply
            for ( final Map.Entry<MetaKey, String> entry : metadata.entrySet () )
            {
                if ( entry.getValue () == null )
                {
                    result.remove ( entry.getKey () );
                }
                else
                {
                    result.put ( entry.getKey (), entry.getValue () );
                }
            }

            // first clear all
            artifact.getProperties ().clear ();
            em.persist ( artifact );
            em.flush ();

            // now add the new set
            convertProperties ( result, artifact, artifact.getProperties () );

            // store
            em.persist ( artifact );

            return result;
        } );
    }

    private Map<MetaKey, String> convert ( final Collection<ArtifactPropertyEntity> properties )
    {
        final Map<MetaKey, String> result = new HashMap<MetaKey, String> ( properties.size () );

        for ( final ArtifactPropertyEntity ape : properties )
        {
            result.put ( new MetaKey ( ape.getNamespace (), ape.getKey () ), ape.getValue () );
        }

        return result;
    }

    @Override
    public Collection<Artifact> findByName ( final String channelId, final String artifactName )
    {
        return doWithTransaction ( em -> {

            final ChannelEntity channel = getCheckedChannel ( em, channelId );

            final TypedQuery<ArtifactEntity> q = em.createQuery ( String.format ( "SELECT a FROM %s AS a WHERE a.name=:artifactName and a.channel.id=:channelId", ArtifactEntity.class.getName () ), ArtifactEntity.class );
            q.setParameter ( "artifactName", artifactName );
            q.setParameter ( "channelId", channelId );

            final ChannelImpl ci = convert ( channel );

            final Collection<Artifact> result = new LinkedList<> ();
            for ( final ArtifactEntity ae : q.getResultList () )
            {
                result.add ( convert ( ci, ae ) );
            }

            return result;
        } );
    }
}
