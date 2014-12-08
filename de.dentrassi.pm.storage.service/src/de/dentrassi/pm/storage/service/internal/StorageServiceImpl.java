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
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;

import org.eclipse.persistence.config.QueryHints;
import org.eclipse.persistence.config.QueryType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.ByteStreams;

import de.dentrassi.pm.aspect.ChannelAspect;
import de.dentrassi.pm.aspect.ChannelAspectInformation;
import de.dentrassi.pm.aspect.ChannelAspectProcessor;
import de.dentrassi.pm.aspect.extract.Extractor;
import de.dentrassi.pm.aspect.listener.ChannelListener;
import de.dentrassi.pm.aspect.virtual.Virtualizer;
import de.dentrassi.pm.common.service.AbstractJpaServiceImpl;
import de.dentrassi.pm.storage.ArtifactInformation;
import de.dentrassi.pm.storage.MetaKey;
import de.dentrassi.pm.storage.jpa.ArtifactEntity;
import de.dentrassi.pm.storage.jpa.ArtifactPropertyEntity;
import de.dentrassi.pm.storage.jpa.ChannelEntity;
import de.dentrassi.pm.storage.jpa.ExtractedArtifactPropertyEntity;
import de.dentrassi.pm.storage.jpa.ProvidedArtifactPropertyEntity;
import de.dentrassi.pm.storage.jpa.StoredArtifactEntity;
import de.dentrassi.pm.storage.jpa.VirtualArtifactEntity;
import de.dentrassi.pm.storage.service.Artifact;
import de.dentrassi.pm.storage.service.ArtifactReceiver;
import de.dentrassi.pm.storage.service.Channel;
import de.dentrassi.pm.storage.service.StorageService;

public class StorageServiceImpl extends AbstractJpaServiceImpl implements StorageService
{

    private class VirtualizerContextImpl implements Virtualizer.Context
    {
        private final ChannelEntity channel;

        private final Path file;

        private final ArtifactInformation info;

        private final EntityManager em;

        private final String namespace;

        private final StoredArtifactEntity artifact;

        private VirtualizerContextImpl ( final ChannelEntity channel, final Path file, final ArtifactInformation info, final EntityManager em, final String namespace, final StoredArtifactEntity artifact )
        {
            this.channel = channel;
            this.file = file;
            this.info = info;
            this.em = em;
            this.namespace = namespace;
            this.artifact = artifact;
        }

        @Override
        public ArtifactInformation getArtifactInformation ()
        {
            return this.info;
        }

        @Override
        public Path getFile ()
        {
            return this.file;
        }

        @Override
        public void createVirtualArtifact ( final String name, final InputStream stream )
        {
            try
            {
                performStoreArtifact ( this.channel, name, stream, this.em, false, ( ) -> {
                    final VirtualArtifactEntity ve = new VirtualArtifactEntity ();
                    ve.setParent ( this.artifact );
                    ve.setNamespace ( this.namespace );
                    return ve;
                }, null );
            }
            catch ( final Exception e )
            {
                throw new RuntimeException ( e );
            }
            finally
            {
                try
                {
                    stream.close ();
                }
                catch ( final IOException e )
                {
                    // ignore this one
                }
            }
        }
    }

    private final static Logger logger = LoggerFactory.getLogger ( StorageServiceImpl.class );

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
    public Channel getChannelWithAlias ( final String channelIdOrName )
    {
        return doWithTransaction ( em -> {
            ChannelEntity channel = em.find ( ChannelEntity.class, channelIdOrName );
            if ( channel == null )
            {
                channel = findByName ( em, channelIdOrName );
            }
            return convert ( channel );
        } );
    }

    protected ChannelEntity findByName ( final EntityManager em, final String channelName )
    {
        final TypedQuery<ChannelEntity> q = em.createQuery ( String.format ( "SELECT c FROM %s AS c WHERE c.name=:name", ChannelEntity.class.getName () ), ChannelEntity.class );
        q.setParameter ( "name", channelName );

        final List<ChannelEntity> result = q.getResultList ();
        if ( result.isEmpty () )
        {
            return null;
        }
        return result.get ( 0 );
    }

    @Override
    public Artifact createArtifact ( final String channelId, final String name, final InputStream stream )
    {
        return createArtifact ( channelId, name, stream, null );
    }

    @Override
    public Artifact createArtifact ( final String channelId, final String name, final InputStream stream, final Map<MetaKey, String> providedMetaData )
    {
        final Artifact artifact;
        try
        {
            artifact = doWithTransaction ( em -> {
                final ChannelEntity channel = getCheckedChannel ( em, channelId );
                return performStoreArtifact ( channel, name, stream, em, true, StoredArtifactEntity::new, providedMetaData );
            } );
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

        return artifact;
    }

    private Artifact performStoreArtifact ( final ChannelEntity channel, final String name, final InputStream stream, final EntityManager em, final boolean triggerVirtual, final Supplier<ArtifactEntity> entityCreator, final Map<MetaKey, String> providedMetaData ) throws Exception
    {
        final Path file = Files.createTempFile ( "blob-", null );

        try
        {
            // copy data to temp file
            try ( BufferedOutputStream os = new BufferedOutputStream ( new FileOutputStream ( file.toFile () ) ) )
            {
                ByteStreams.copy ( stream, os );
            }

            {
                final PreAddContentImpl context = new PreAddContentImpl ( name, file );
                runChannelTriggers ( em, channel, listener -> listener.artifactPreAdd ( context ) );
                if ( context.isVeto () )
                {
                    logger.info ( "Veto add artifact {} to channel {}", name, channel.getId () );
                    return null;
                }
            }

            final Map<MetaKey, String> metadata = extractMetaData ( em, channel, file );

            ArtifactEntity ae;
            try ( BufferedInputStream in = new BufferedInputStream ( new FileInputStream ( file.toFile () ) ) )
            {
                ae = storeBlob ( entityCreator, em, channel, name, in, metadata, providedMetaData );
            }

            if ( ae instanceof StoredArtifactEntity )
            {
                createVirtualArtifacts ( em, channel, (StoredArtifactEntity)ae, file );
            }

            final Artifact a = convert ( convert ( channel ), ae );

            // now run the post add trigger
            runChannelTriggers ( em, channel, listener -> listener.artifactAdded ( new AddedContextImpl ( a, metadata, file ) ) );

            return a;
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
                logger.info ( "Failed to delete temp file", e );
                // ignore this
            }
        }
    }

    private void createVirtualArtifacts ( final EntityManager em, final ChannelEntity channel, final StoredArtifactEntity artifact, final Path file )
    {
        Activator.getChannelAspects ().processWithAspect ( channel.getAspects (), ChannelAspect::getArtifactVirtualizer, ( aspect, virtualizer ) -> virtualizer.virtualize ( createVirtualContext ( em, channel, artifact, file, aspect.getId () ) ) );
    }

    private VirtualizerContextImpl createVirtualContext ( final EntityManager em, final ChannelEntity channel, final StoredArtifactEntity artifact, final Path file, final String namespace )
    {
        final ArtifactInformation info = convert ( artifact );
        return new VirtualizerContextImpl ( channel, file, info, em, namespace, artifact );
    }

    protected void runChannelTriggers ( final EntityManager em, final ChannelEntity channel, final Consumer<ChannelListener> listener )
    {
        Activator.getChannelAspects ().process ( channel.getAspects (), ChannelAspect::getChannelListener, listener );
    }

    protected Map<MetaKey, String> extractMetaData ( final EntityManager em, final ChannelEntity channel, final Path file )
    {
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

    protected ArtifactEntity storeBlob ( final Supplier<ArtifactEntity> artifactSupplier, final EntityManager em, final ChannelEntity channel, final String name, final InputStream stream, final Map<MetaKey, String> extractedMetaData, final Map<MetaKey, String> providedMetaData ) throws SQLException, IOException
    {
        final ArtifactEntity artifact = artifactSupplier.get ();
        artifact.setName ( name );
        artifact.setChannel ( channel );

        convertExtractedProperties ( extractedMetaData, artifact, artifact.getExtractedProperties () );
        convertProvidedProperties ( providedMetaData, artifact, artifact.getProvidedProperties () );

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

        return artifact;
    }

    private <T extends ArtifactPropertyEntity> T fillPropertyEntry ( final ArtifactEntity artifact, final Map.Entry<MetaKey, String> entry, final Supplier<T> supplier )
    {
        final T ap = supplier.get ();
        ap.setArtifact ( artifact );
        ap.setKey ( entry.getKey ().getKey () );
        ap.setNamespace ( entry.getKey ().getNamespace () );
        ap.setValue ( entry.getValue () );
        return ap;
    }

    private void convertProvidedProperties ( final Map<MetaKey, String> metadata, final ArtifactEntity artifact, final Collection<ProvidedArtifactPropertyEntity> props )
    {
        if ( metadata != null )
        {
            metadata.entrySet ().stream ().map ( entry -> fillPropertyEntry ( artifact, entry, ProvidedArtifactPropertyEntity::new ) ).collect ( Collectors.toCollection ( ( ) -> props ) );
        }
    }

    private void convertExtractedProperties ( final Map<MetaKey, String> metadata, final ArtifactEntity artifact, final Collection<ExtractedArtifactPropertyEntity> props )
    {
        if ( metadata != null )
        {
            metadata.entrySet ().stream ().map ( entry -> fillPropertyEntry ( artifact, entry, ExtractedArtifactPropertyEntity::new ) ).collect ( Collectors.toCollection ( ( ) -> props ) );
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

            final ChannelImpl channel = convert ( ce );

            final TypedQuery<ArtifactEntity> query = em.createQuery ( String.format ( "select a from %s a WHERE a.channel=:channel", ArtifactEntity.class.getName () ), ArtifactEntity.class );
            query.setParameter ( "channel", ce );

            query.setHint ( QueryHints.QUERY_TYPE, QueryType.ReadAll );
            query.setHint ( "eclipselink.batch", "a.channel" );

            /*
            query.setHint ( "eclipselink.join-fetch", "a.channel" );
            query.setHint ( "eclipselink.join-fetch", "a.extractedProperties" );
            query.setHint ( "eclipselink.join-fetch", "a.providedProperties" );

            query.setHint ( QueryHints.QUERY_TYPE, QueryType.ReadAll );
            */

            final Set<Artifact> result = new TreeSet<> ();
            for ( final ArtifactEntity ae : query.getResultList () )
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
        return new ChannelImpl ( ce.getId (), ce.getName (), this );
    }

    private Artifact convert ( final ChannelImpl channel, final ArtifactEntity ae )
    {
        if ( ae == null )
        {
            return null;
        }

        final Map<MetaKey, String> metadata = convertMetaData ( ae );

        return new ArtifactImpl ( channel, ae.getId (), ae.getName (), ae.getSize (), metadata, ae instanceof VirtualArtifactEntity );
    }

    private SortedMap<MetaKey, String> convertMetaData ( final ArtifactEntity ae )
    {
        final SortedMap<MetaKey, String> metadata = new TreeMap<> ();

        for ( final ArtifactPropertyEntity entry : ae.getExtractedProperties () )
        {
            metadata.put ( new MetaKey ( entry.getNamespace (), entry.getKey () ), entry.getValue () );
        }

        for ( final ArtifactPropertyEntity entry : ae.getProvidedProperties () )
        {
            metadata.put ( new MetaKey ( entry.getNamespace (), entry.getKey () ), entry.getValue () );
        }

        return metadata;
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
        final String artifactId = ae.getId ();

        final Connection c = em.unwrap ( Connection.class );
        try ( PreparedStatement ps = c.prepareStatement ( "select DATA from ARTIFACTS where ID=?" ) )
        {
            ps.setObject ( 1, artifactId );
            try ( ResultSet rs = ps.executeQuery () )
            {
                if ( !rs.next () )
                {
                    throw new FileNotFoundException ( String.format ( "Data for artifact '%s' not found", artifactId ) );
                }

                final Blob blob = rs.getBlob ( 1 );
                try ( InputStream stream = blob.getBinaryStream () )
                {
                    receiver.receive ( convert ( ae ), stream );
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

            if ( ae instanceof VirtualArtifactEntity && ( (VirtualArtifactEntity)ae ).getParent () != null )
            {
                throw new IllegalStateException ( String.format ( "Unable to delete virtual artifact of %s (%s)", ae.getName (), ae.getId () ) );
            }

            final ArtifactInformation info = convert ( ae );

            final String name = ae.getName ();
            final Map<MetaKey, String> metadata = convertMetaData ( ae );

            final ChannelEntity channel = ae.getChannel ();

            em.remove ( ae );
            em.flush ();

            // now run the post add trigger

            runChannelTriggers ( em, channel, listener -> listener.artifactRemoved ( new RemovedContextImpl ( artifactId, name, metadata ) ) );

            return info;
        } );
    }

    private ArtifactInformation convert ( final ArtifactEntity ae )
    {
        return new ArtifactInformation ( ae.getId (), ae.getSize (), ae.getName (), ae.getChannel ().getId (), convertMetaData ( ae ) );
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
        logger.info ( "Reprocessing aspect - channelId: {}, aspect: {}", channel.getId (), aspectFactoryId );

        for ( final ArtifactEntity ae : channel.getArtifacts () )
        {
            /*
            if ( ! ( ae instanceof StoredArtifactEntity ) )
            {
                continue;
            }
            */

            logger.debug ( "Reprocessing artifact - {}", ae.getId () );

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
                    final List<String> list = Arrays.asList ( aspectFactoryId );
                    ca.process ( list, ChannelAspect::getExtractor, extractor -> {
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

                    // add metadata first, since the virtualizers might need it

                    convertExtractedProperties ( metadata, ae, ae.getExtractedProperties () );

                    // process virtual

                    if ( ae instanceof StoredArtifactEntity )
                    {
                        ca.process ( list, ChannelAspect::getArtifactVirtualizer, virtualizer -> virtualizer.virtualize ( createVirtualContext ( em, channel, (StoredArtifactEntity)ae, file, aspectFactoryId ) ) );
                    }

                    // store

                    em.persist ( ae );
                    em.flush ();
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

            {
                final Query q = em.createQuery ( String.format ( "DELETE from %s ap where ap.namespace=:factoryId and ap.artifact.channel.id=:channelId", ExtractedArtifactPropertyEntity.class.getSimpleName () ) );
                q.setParameter ( "factoryId", aspectFactoryId );
                q.setParameter ( "channelId", channelId );
                q.executeUpdate ();
            }

            {
                final Query q = em.createQuery ( String.format ( "DELETE from %s ap where ap.namespace=:factoryId and ap.artifact.channel.id=:channelId", ProvidedArtifactPropertyEntity.class.getSimpleName () ) );
                q.setParameter ( "factoryId", aspectFactoryId );
                q.setParameter ( "channelId", channelId );
                q.executeUpdate ();
            }

            {
                final Query q = em.createQuery ( String.format ( "DELETE from %s va where va.namespace=:factoryId and va.channel.id=:channelId", VirtualArtifactEntity.class.getSimpleName () ) );
                q.setParameter ( "factoryId", aspectFactoryId );
                q.setParameter ( "channelId", channelId );
                q.executeUpdate ();
            }

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
            final Map<MetaKey, String> result = convert ( artifact.getProvidedProperties () );

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
            artifact.getProvidedProperties ().clear ();
            em.persist ( artifact );
            em.flush ();

            // now add the new set
            convertProvidedProperties ( result, artifact, artifact.getProvidedProperties () );

            // store
            em.persist ( artifact );

            return result;
        } );
    }

    private Map<MetaKey, String> convert ( final Collection<? extends ArtifactPropertyEntity> properties )
    {
        final Map<MetaKey, String> result = new HashMap<MetaKey, String> ( properties.size () );

        for ( final ArtifactPropertyEntity ape : properties )
        {
            result.put ( new MetaKey ( ape.getNamespace (), ape.getKey () ), ape.getValue () );
        }

        return result;
    }

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

    @Override
    public void clearChannel ( final String channelId )
    {
        doWithTransactionVoid ( em -> {
            final Query q = em.createQuery ( String.format ( "DELETE from %s ae where ae.channel.id=:channelId", ArtifactEntity.class.getName () ) );
            q.setParameter ( "channelId", channelId );
            q.executeUpdate ();
        } );
    }

    @Override
    public void updateChannel ( final String channelId, final String name )
    {
        doWithTransactionVoid ( em -> {
            final ChannelEntity channel = getCheckedChannel ( em, channelId );

            if ( "".equals ( name ) )
            {
                channel.setName ( null );
            }
            else
            {
                channel.setName ( name );
            }

            em.persist ( channel );
        } );
    }

}
