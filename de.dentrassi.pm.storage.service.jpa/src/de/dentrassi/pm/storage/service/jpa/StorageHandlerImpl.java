/*******************************************************************************
 * Copyright (c) 2014, 2015 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package de.dentrassi.pm.storage.service.jpa;

import static de.dentrassi.pm.storage.service.jpa.StreamServiceHelper.convert;
import static de.dentrassi.pm.storage.service.jpa.StreamServiceHelper.convertDetailed;
import static de.dentrassi.pm.storage.service.jpa.StreamServiceHelper.convertMetaData;
import static de.dentrassi.pm.storage.service.jpa.StreamServiceHelper.createTempFile;
import static de.dentrassi.pm.storage.service.jpa.StreamServiceHelper.isDeleteable;
import static de.dentrassi.pm.storage.service.jpa.StreamServiceHelper.testLocked;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.io.ByteStreams;

import de.dentrassi.pm.aspect.ChannelAspect;
import de.dentrassi.pm.aspect.ChannelAspectProcessor;
import de.dentrassi.pm.aspect.aggregate.AggregationContext;
import de.dentrassi.pm.aspect.listener.ChannelListener;
import de.dentrassi.pm.aspect.listener.PostAddContext;
import de.dentrassi.pm.aspect.virtual.Virtualizer;
import de.dentrassi.pm.common.ArtifactInformation;
import de.dentrassi.pm.common.ChannelAspectInformation;
import de.dentrassi.pm.common.DetailedArtifactInformation;
import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.common.event.AddedEvent;
import de.dentrassi.pm.common.event.RemovedEvent;
import de.dentrassi.pm.common.utils.IOConsumer;
import de.dentrassi.pm.common.utils.ThrowingConsumer;
import de.dentrassi.pm.generator.GenerationContext;
import de.dentrassi.pm.generator.GeneratorProcessor;
import de.dentrassi.pm.storage.ArtifactReceiver;
import de.dentrassi.pm.storage.CacheEntry;
import de.dentrassi.pm.storage.CacheEntryInformation;
import de.dentrassi.pm.storage.StorageAccessor;
import de.dentrassi.pm.storage.jpa.ArtifactEntity;
import de.dentrassi.pm.storage.jpa.ArtifactEntity_;
import de.dentrassi.pm.storage.jpa.ArtifactPropertyEntity;
import de.dentrassi.pm.storage.jpa.AttachedArtifactEntity;
import de.dentrassi.pm.storage.jpa.ChannelCacheEntity;
import de.dentrassi.pm.storage.jpa.ChannelCacheKey;
import de.dentrassi.pm.storage.jpa.ChannelEntity;
import de.dentrassi.pm.storage.jpa.ExtractedArtifactPropertyEntity;
import de.dentrassi.pm.storage.jpa.GeneratedArtifactEntity;
import de.dentrassi.pm.storage.jpa.GeneratorArtifactEntity;
import de.dentrassi.pm.storage.jpa.ProvidedArtifactPropertyEntity;
import de.dentrassi.pm.storage.jpa.ProvidedChannelPropertyEntity;
import de.dentrassi.pm.storage.jpa.StoredArtifactEntity;
import de.dentrassi.pm.storage.jpa.VirtualArtifactEntity;
import de.dentrassi.pm.storage.service.jpa.blob.BlobStore;

public class StorageHandlerImpl extends AbstractHandler implements StorageAccessor, StreamServiceHelper
{

    private final static Logger logger = LoggerFactory.getLogger ( StorageHandlerImpl.class );

    public class AggregationContextImpl implements AggregationContext
    {
        private final Collection<ArtifactInformation> artifacts;

        private final SortedMap<MetaKey, String> metaData;

        private final ChannelEntity channel;

        private final String namespace;

        public AggregationContextImpl ( final Collection<ArtifactInformation> artifacts, final SortedMap<MetaKey, String> metaData, final ChannelEntity channel, final String namespace )
        {
            this.artifacts = artifacts;
            this.metaData = metaData;
            this.channel = channel;
            this.namespace = namespace;
        }

        @Override
        public String getChannelId ()
        {
            return this.channel.getId ();
        }

        @Override
        public String getChannelName ()
        {
            return this.channel.getName ();
        }

        @Override
        public String getChannelDescription ()
        {
            return this.channel.getDescription ();
        }

        @Override
        public Collection<ArtifactInformation> getArtifacts ()
        {
            return this.artifacts;
        }

        @Override
        public Map<MetaKey, String> getChannelMetaData ()
        {
            return this.metaData;
        }

        @Override
        public void createCacheEntry ( final String id, final String name, final String mimeType, final IOConsumer<OutputStream> creator )
        {
            try
            {
                internalCreateCacheEntry ( this.channel, this.namespace, id, name, mimeType, creator );
            }
            catch ( final IOException e )
            {
                throw new RuntimeException ( e );
            }
        }

        @Override
        public void streamArtifact ( final String artifactId, final ArtifactReceiver receiver ) throws FileNotFoundException
        {
            final ArtifactEntity art = StorageHandlerImpl.this.em.find ( ArtifactEntity.class, artifactId );
            if ( art == null )
            {
                throw new FileNotFoundException ( artifactId );
            }
            try
            {
                StorageHandlerImpl.this.blobStore.streamArtifact ( StorageHandlerImpl.this.em, art, receiver );
            }
            catch ( final Exception e )
            {
                throw new RuntimeException ( e );
            }
        }

    }

    private class ArtifactContextImpl implements Virtualizer.Context, GenerationContext
    {
        private final ChannelEntity channel;

        private final Path file;

        private final Supplier<ArtifactInformation> infoSupplier;

        private final EntityManager em;

        private final Supplier<ArtifactEntity> entitySupplier;

        private final boolean runAggregator;

        private ArtifactInformation info;

        private final RegenerateTracker tracker;

        private ArtifactContextImpl ( final ChannelEntity channel, final RegenerateTracker tracker, final boolean runAggregator, final Path file, final Supplier<ArtifactInformation> infoSupplier, final EntityManager em, final Supplier<ArtifactEntity> entitySupplier )
        {
            this.channel = channel;
            this.file = file;
            this.infoSupplier = infoSupplier;
            this.em = em;
            this.entitySupplier = entitySupplier;
            this.runAggregator = runAggregator;
            this.tracker = tracker;
        }

        @Override
        public ArtifactInformation getOtherArtifactInformation ( final String artifactId )
        {
            if ( artifactId == null )
            {
                return null;
            }

            return convert ( this.em.find ( ArtifactEntity.class, artifactId ), null );
        }

        @Override
        public ArtifactInformation getArtifactInformation ()
        {
            if ( this.info == null )
            {
                this.info = this.infoSupplier.get ();
            }
            return this.info;
        }

        @Override
        public Path getFile ()
        {
            return this.file;
        }

        @Override
        public void createVirtualArtifact ( final String name, final InputStream stream, final Map<MetaKey, String> providedMetaData )
        {
            try
            {
                performStoreArtifact ( this.channel, name, stream, this.entitySupplier, providedMetaData, this.tracker, this.runAggregator, false );
            }
            catch ( final Exception e )
            {
                throw new RuntimeException ( e );
            }
        }

        @Override
        public StorageAccessor getStorage ()
        {
            return StorageHandlerImpl.this;
        }
    }

    private final ChannelAspectProcessor channelAspectProcessor = Activator.getChannelAspects ();

    private final BlobStore blobStore;

    private final GeneratorProcessor generatorProcessor;

    public StorageHandlerImpl ( final EntityManager em, final GeneratorProcessor generatorProcessor, final LockManager<String> lockManager, final BlobStore blobStore )
    {
        super ( em, lockManager );
        this.blobStore = blobStore;
        this.generatorProcessor = generatorProcessor;
    }

    public ChannelEntity createChannel ( final String name, final String description, final Map<MetaKey, String> providedMetaData )
    {
        final ChannelEntity channel = new ChannelEntity ();
        channel.setName ( name );
        channel.setDescription ( description );

        setProvidedMetaData ( channel, providedMetaData );

        this.em.persist ( channel );

        return channel;
    }

    public void setProvidedMetaData ( final ChannelEntity channel, final Map<MetaKey, String> providedMetaData )
    {
        if ( providedMetaData == null )
        {
            return;
        }

        channel.getProvidedProperties ().clear ();

        for ( final Map.Entry<MetaKey, String> entry : providedMetaData.entrySet () )
        {
            final ProvidedChannelPropertyEntity pe = new ProvidedChannelPropertyEntity ();
            pe.setNamespace ( entry.getKey ().getNamespace () );
            pe.setKey ( entry.getKey ().getKey () );
            pe.setValue ( entry.getValue () );
            pe.setChannel ( channel );
            channel.getProvidedProperties ().add ( pe );
        }

        this.em.persist ( channel );
    }

    @Override
    public void updateChannel ( final String channelId, final String name, final String description )
    {
        final ChannelEntity channel = getCheckedChannel ( channelId );

        if ( "".equals ( name ) )
        {
            channel.setName ( null );
        }
        else
        {
            channel.setName ( name );
        }

        channel.setDescription ( description );

        this.em.persist ( channel );
    }

    @Override
    public void regenerateAll ( final String channelId )
    {
        scanArtifacts ( channelId, ( ae ) -> {
            if ( ae instanceof GeneratorArtifactEntity )
            {
                try
                {
                    regenerateArtifact ( (GeneratorArtifactEntity)ae, true );
                }
                catch ( final Exception e )
                {
                    throw new RuntimeException ( e );
                }
            }
        } );
    }

    public void regenerateArtifact ( final GeneratorArtifactEntity ae, final boolean runAggregator ) throws Exception
    {
        this.lockManager.modifyRun ( ae.getChannel ().getId (), () -> {
            this.blobStore.doStreamed ( this.em, ae, ( file ) -> {
                // first clear old generated artifacts

                deleteGeneratedChildren ( ae );
                this.em.flush ();

                generateArtifact ( ae.getChannel (), ae, file, runAggregator );
                this.em.flush ();
            } );
        } );
    }

    protected void deleteAllWithParent ( final Class<?> type, final ArtifactEntity parent )
    {
        final Query q = this.em.createQuery ( String.format ( "SELECT ent from %s ent where ent.parent=:parent", type.getName () ) );
        q.setParameter ( "parent", parent );

        deleteResult ( q );
    }

    protected void deleteGeneratedChildren ( final GeneratorArtifactEntity artifact )
    {
        deleteAllWithParent ( GeneratedArtifactEntity.class, artifact );
    }

    protected void deleteVirtualChildren ( final ArtifactEntity artifact )
    {
        deleteAllWithParent ( VirtualArtifactEntity.class, artifact );
    }

    private void deleteAllVirtualArtifacts ( final ChannelEntity channel )
    {
        final Query q = this.em.createQuery ( String.format ( "SELECT va from %s va where va.channel=:channel", VirtualArtifactEntity.class.getName () ) );
        q.setParameter ( "channel", channel );

        final int result = deleteResult ( q );

        logger.info ( "Deleted {} artifacts in channel {}", result, channel.getId () );
    }

    public int deleteResult ( final Query q )
    {
        final List<?> result = q.getResultList ();
        for ( final Object art : result )
        {
            this.em.remove ( art );
        }
        this.em.flush ();
        return result.size ();
    }

    public void generateArtifact ( final ChannelEntity channel, final GeneratorArtifactEntity ae, final Path file, final boolean runAggregator ) throws Exception
    {
        final String generatorId = ae.getGeneratorId ();

        final RegenerateTracker tracker = new RegenerateTracker ();

        final ArtifactContextImpl ctx = createGeneratedContext ( tracker, this.em, channel, ae, file, runAggregator );
        this.generatorProcessor.process ( generatorId, ctx );

        tracker.process ( this );
    }

    private ArtifactContextImpl createGeneratedContext ( final RegenerateTracker tracker, final EntityManager em, final ChannelEntity channel, final ArtifactEntity artifact, final Path file, final boolean runAggregator )
    {
        return new ArtifactContextImpl ( channel, tracker, runAggregator, file, () -> convert ( artifact, null ), em, () -> {
            final GeneratedArtifactEntity ge = new GeneratedArtifactEntity ();
            ge.setParent ( artifact );
            artifact.getChildArtifacts ().add ( ge );
            return ge;
        } );
    }

    public ArtifactEntity performStoreArtifact ( final ChannelEntity channel, final String name, final InputStream stream, final Supplier<ArtifactEntity> entityCreator, final Map<MetaKey, String> providedMetaData, final RegenerateTracker tracker, final boolean runAggregator, final boolean external ) throws Exception
    {
        logger.debug ( "Storing artifact: {} in channel: {}", name, channel.getId () );

        final Path file = createTempFile ( name );

        try
        {
            // copy data to temp file
            try ( BufferedOutputStream os = new BufferedOutputStream ( new FileOutputStream ( file.toFile () ) ) )
            {
                ByteStreams.copy ( stream, os );
            }

            {
                final PreAddContentImpl context = new PreAddContentImpl ( name, file, channel.getId (), external );
                runChannelTriggers ( tracker, channel, listener -> listener.artifactPreAdd ( context ), null );
                if ( context.isVeto () )
                {
                    logger.info ( "Veto add artifact {} to channel {}", name, channel.getId () );
                    return null;
                }
            }

            final SortedMap<MetaKey, String> metadata = extractMetaData ( this.em, channel, file );

            return this.lockManager.modifyCall ( channel.getId (), () -> {

                final ArtifactEntity ae = entityCreator.get ();
                ae.setName ( name );
                ae.setChannel ( channel );
                ae.setCreationTimestamp ( new Date () );

                Helper.convertExtractedProperties ( metadata, ae, ae.getExtractedProperties () );
                Helper.convertProvidedProperties ( providedMetaData, ae, ae.getProvidedProperties () );

                try ( BufferedInputStream in = new BufferedInputStream ( new FileInputStream ( file.toFile () ) ) )
                {
                    this.blobStore.storeBlob ( this.em, in, size -> {
                        ae.setSize ( size );
                        this.em.persist ( ae );
                        this.em.flush ();
                        return ae.getId ();
                    } );
                }

                if ( ae instanceof GeneratorArtifactEntity )
                {
                    generateArtifact ( channel, (GeneratorArtifactEntity)ae, file, runAggregator );
                }

                final AddedEvent event = new AddedEvent ( ae.getId (), metadata );
                runChannelTriggers ( tracker, channel, listener -> listener.artifactAdded ( createPostAddContext ( channel.getId () ) ), event );

                createVirtualArtifacts ( channel, ae, file, tracker, runAggregator );

                // now run the channel aggregator if requested
                if ( runAggregator )
                {
                    runChannelAggregators ( channel );
                }

                return ae;
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
                logger.info ( "Failed to delete temp file", e );
                // ignore this
            }
        }
    }

    private PostAddContext createPostAddContext ( final String channelId )
    {
        return new PostAddContentImpl ( this, channelId );
    }

    protected void runChannelTriggers ( final RegenerateTracker tracker, final ChannelEntity channel, final ThrowingConsumer<ChannelListener> listener, final Object event )
    {
        Activator.getChannelAspects ().process ( channel.getAspects ().keySet (), ChannelAspect::getChannelListener, ( t ) -> {
            try
            {
                listener.accept ( t );
            }
            catch ( final Exception e )
            {
                throw new RuntimeException ( e );
            }
        } );

        if ( event != null )
        {
            runGeneratorTriggers ( tracker, channel, event );
        }
    }

    protected static SortedMap<MetaKey, String> extractMetaData ( final EntityManager em, final ChannelEntity channel, final Path file )
    {
        final SortedMap<MetaKey, String> metadata = new TreeMap<> ();

        Activator.getChannelAspects ().process ( channel.getAspects ().keySet (), ChannelAspect::getExtractor, extractor -> {
            try
            {
                final Map<String, String> md = new HashMap<> ();
                extractor.extractMetaData ( file, md );

                convertMetaDataFromExtractor ( metadata, extractor.getAspect ().getId (), md );
            }
            catch ( final Exception e )
            {
                throw new RuntimeException ( e );
            }
        } );

        return metadata;
    }

    private static void convertMetaDataFromExtractor ( final Map<MetaKey, String> metadata, final String namespace, final Map<String, String> md )
    {
        if ( md == null )
        {
            return;
        }

        for ( final Map.Entry<String, String> mde : md.entrySet () )
        {
            metadata.put ( new MetaKey ( namespace, mde.getKey () ), mde.getValue () );
        }
    }

    private void createVirtualArtifacts ( final ChannelEntity channel, final ArtifactEntity artifact, final Path file, final RegenerateTracker tracker, final boolean runAggregator )
    {
        logger.debug ( "Creating virtual artifacts for - channel: {}, artifact: {}, runAggregator: {}", channel.getId (), artifact.getId (), runAggregator );

        Activator.getChannelAspects ().processWithAspect ( channel.getAspects ().keySet (), ChannelAspect::getArtifactVirtualizer, ( aspect, virtualizer ) -> virtualizer.virtualize ( createArtifactContext ( this.em, channel, artifact, file, aspect.getId (), tracker, runAggregator ) ) );
    }

    private ArtifactContextImpl createArtifactContext ( final EntityManager em, final ChannelEntity channel, final ArtifactEntity artifact, final Path file, final String namespace, final RegenerateTracker tracker, final boolean runAggregator )
    {
        logger.debug ( "Creating virtual artifact context for: {}", namespace );

        return new ArtifactContextImpl ( channel, tracker, runAggregator, file, () -> convert ( artifact, null ), em, () -> {
            final VirtualArtifactEntity ve = new VirtualArtifactEntity ();

            ve.setParent ( artifact );
            artifact.getChildArtifacts ().add ( ve );

            ve.setNamespace ( namespace );
            return ve;
        } );
    }

    public void generateArtifact ( final String id )
    {
        try
        {
            final ArtifactEntity ae = this.em.find ( ArtifactEntity.class, id );
            if ( ae == null )
            {
                throw new IllegalArgumentException ( String.format ( "Unable to find artifact '%s' ", id ) );
            }
            if ( ! ( ae instanceof GeneratorArtifactEntity ) )
            {
                throw new IllegalArgumentException ( String.format ( "Artifact '%s' is not a generator artifact.", id ) );
            }

            testLocked ( ae.getChannel () );

            regenerateArtifact ( (GeneratorArtifactEntity)ae, false );
            runChannelAggregators ( ae.getChannel () );
        }
        catch ( final Exception e )
        {
            throw new RuntimeException ( e );
        }
    }

    public ArtifactInformation deleteArtifact ( final String artifactId )
    {
        logger.debug ( "Request to delete artifact: {}", artifactId );

        final ArtifactEntity ae = this.em.find ( ArtifactEntity.class, artifactId );
        if ( ae == null )
        {
            return null; // silently ignore
        }

        testLocked ( ae.getChannel () );

        if ( !isDeleteable ( ae ) )
        {
            throw new IllegalStateException ( String.format ( "Unable to delete artifact %s (%s). Artifact might be virtual or generated.", ae.getName (), ae.getId () ) );
        }

        final SortedMap<MetaKey, String> md = convertMetaData ( ae );

        this.em.remove ( ae );
        this.em.flush ();

        logger.info ( "Artifact deleted: {}", artifactId );

        final ChannelEntity channel = ae.getChannel ();

        final RegenerateTracker tracker = new RegenerateTracker ();
        runGeneratorTriggers ( tracker, channel, new RemovedEvent ( ae.getId (), md ) );
        tracker.process ( this );

        // now run the channel aggregator
        runChannelAggregators ( channel );

        return convert ( ae, null );
    }

    private void runGeneratorTriggers ( final RegenerateTracker tracker, final ChannelEntity channel, final Object event )
    {
        scanArtifacts ( channel, ae -> {

            // TODO: scanArtifacts should allow us to search for a specific artifact type

            if ( ! ( ae instanceof GeneratorArtifactEntity ) )
            {
                return;
            }

            final String gid = ( (GeneratorArtifactEntity)ae ).getGeneratorId ();

            logger.debug ( "Checking generator artifact {} / {} if regeneration is required", ae.getId (), gid );

            this.generatorProcessor.process ( gid, ( generator ) -> {
                if ( generator.shouldRegenerate ( event ) )
                {
                    logger.debug ( "Need to re-generate artifact {} with generator {}", ae.getId (), gid );
                    tracker.add ( (GeneratorArtifactEntity)ae );
                };
            } );
        } );
    }

    public void runChannelAggregators ( final ChannelEntity channel )
    {
        logger.info ( "Running channel aggregators - channelId: {}", channel.getId () );

        this.lockManager.modifyRun ( channel.getId (), () -> {

            // delete old cache entries

            deleteAllCacheEntries ( channel );

            // flush

            this.em.persist ( channel );
            this.em.flush ();

            // current state for context

            final Collection<ArtifactInformation> artifacts = getArtifacts ( channel );
            final SortedMap<MetaKey, String> metaData = convertMetaData ( null, channel.getProvidedProperties () );

            // gather new meta data
            final Map<MetaKey, String> metadata = new HashMap<> ();

            this.channelAspectProcessor.process ( channel.getAspects ().keySet (), ChannelAspect::getChannelAggregator, aggregator -> {
                try
                {
                    // create new context for this channel aspect
                    final AggregationContext context = new AggregationContextImpl ( artifacts, metaData, channel, aggregator.getId () );

                    // process
                    final Map<String, String> md = aggregator.aggregateMetaData ( context );
                    convertMetaDataFromExtractor ( metadata, aggregator.getId (), md );
                }
                catch ( final Exception e )
                {
                    throw new RuntimeException ( String.format ( "Failed to run channel aggregator: %s", aggregator.getId () ), e );
                }
            } );

            // clear old meta data

            channel.getExtractedProperties ().clear ();
            this.em.persist ( channel );
            this.em.flush ();

            // set new meta data

            Helper.convertExtractedProperties ( metadata, channel, channel.getExtractedProperties () );

            this.em.persist ( channel );
            this.em.flush ();
        } );
    }

    public void runChannelAggregators ( final String channelId )
    {
        runChannelAggregators ( getCheckedChannel ( channelId ) );
    }

    protected void reprocessAspects ( final ChannelEntity channel, final Set<String> aspectFactoryIds ) throws Exception
    {
        logger.info ( "Reprocessing aspect - channelId: {}, aspects: {}", channel.getId (), aspectFactoryIds );

        if ( aspectFactoryIds.isEmpty () )
        {
            // nothing to do
            return;
        }

        final RegenerateTracker tracker = new RegenerateTracker ();

        // first delete all virtual artifacts

        deleteAllVirtualArtifacts ( channel );

        // delete all metadata first

        {
            final Query q = this.em.createQuery ( String.format ( "DELETE from %s eap where eap.namespace in :ASPECT and eap.artifact.channel=:CHANNEL", ExtractedArtifactPropertyEntity.class.getName () ) );
            q.setParameter ( "ASPECT", aspectFactoryIds );
            q.setParameter ( "CHANNEL", channel );
            q.executeUpdate ();
        }

        // process new meta data

        for ( final ArtifactEntity ae : channel.getArtifacts () )
        {
            if ( ae instanceof GeneratorArtifactEntity )
            {
                continue;
            }

            logger.debug ( "Reprocessing artifact - {}", ae.getId () );

            this.blobStore.doStreamed ( this.em, ae, ( file ) -> {
                // generate metadata for new factory

                final Map<MetaKey, String> metadata = new HashMap<> ();
                this.channelAspectProcessor.process ( aspectFactoryIds, ChannelAspect::getExtractor, extractor -> {
                    try
                    {
                        final Map<String, String> md = new HashMap<> ();
                        extractor.extractMetaData ( file, md );
                        convertMetaDataFromExtractor ( metadata, extractor.getAspect ().getId (), md );
                    }
                    catch ( final Exception e )
                    {
                        throw new RuntimeException ( e );
                    }
                } );

                // don't clear extracted meta data, since we only process one aspect and we actually add it

                Helper.convertExtractedProperties ( metadata, ae, ae.getExtractedProperties () );

                // store

                this.em.persist ( ae );
                this.em.flush ();
            } );

        }

        // re-create virtual artifacts

        createAllVirtualArtifacts ( channel, tracker, false );

        tracker.process ( this );

        runChannelAggregators ( channel );

        for ( final ChannelAspectInformation aspect : this.channelAspectProcessor.resolve ( aspectFactoryIds ) )
        {
            channel.getAspects ().put ( aspect.getFactoryId (), aspect.getVersion ().toString () );
        }

        this.em.persist ( channel );
        this.em.flush ();
    }

    public void scanArtifacts ( final String channelId, final ThrowingConsumer<ArtifactEntity> consumer )
    {
        final ChannelEntity ce = this.em.find ( ChannelEntity.class, channelId );

        if ( ce == null )
        {
            throw new IllegalArgumentException ( String.format ( "Channel %s not found", channelId ) );
        }

        scanArtifacts ( ce, consumer );
    }

    protected void scanArtifacts ( final ChannelEntity ce, final ThrowingConsumer<ArtifactEntity> consumer )
    {
        final CriteriaBuilder cb = this.em.getCriteriaBuilder ();
        final CriteriaQuery<ArtifactEntity> cq = cb.createQuery ( ArtifactEntity.class );

        // query

        final Root<ArtifactEntity> root = cq.from ( ArtifactEntity.class );
        final Predicate where = cb.equal ( root.get ( ArtifactEntity_.channel ), ce );

        // fetch

        // root.fetch ( ArtifactEntity_.channel );
        // root.fetch ( ArtifactEntity_.providedProperties );
        // root.fetch ( ArtifactEntity_.extractedProperties );

        // select

        cq.select ( root ).where ( where );

        // convert

        final TypedQuery<ArtifactEntity> query = this.em.createQuery ( cq );

        /*
            query.setHint ( "eclipselink.join-fetch", "ArtifactEntity.providedProperties" );
            query.setHint ( "eclipselink.join-fetch", "ArtifactEntity.extractedProperties" );
            query.setHint ( "eclipselink.join-fetch", "ArtifactEntity.childIds" );
        
            query.setHint ( "eclipselink.batch", "ArtifactEntity.extractedProperties" );
            query.setHint ( "eclipselink.batch", "ArtifactEntity.providedProperties" );
        */

        // final TypedQuery<ArtifactEntity> query = this.em.createQuery ( String.format ( "select a from %s a LEFT JOIN FETCH a.channel WHERE a.channel=:channel ", ArtifactEntity.class.getName () ), ArtifactEntity.class );
        // query.setParameter ( "channel", ce );

        logger.trace ( "Pre get" );

        final List<ArtifactEntity> list = query.getResultList ();

        logger.trace ( "Got result" );

        for ( final ArtifactEntity ae : list )
        {
            try
            {
                consumer.accept ( ae );
            }
            catch ( final Exception e )
            {
                throw new RuntimeException ( e );
            }
        }

        logger.trace ( "Scan complete" );
    }

    public <T extends Comparable<? super T>> Set<T> listArtifacts ( final ChannelEntity ce, final Function<ArtifactEntity, T> mapper )
    {
        final Set<T> result = new TreeSet<> ();
        scanArtifacts ( ce, ( ae ) -> {
            final T t = mapper.apply ( ae );
            if ( t != null )
            {
                result.add ( t );
            }
        } );
        return result;
    }

    @Override
    public Set<ArtifactInformation> getArtifacts ( final String channelId )
    {
        return getArtifacts ( getCheckedChannel ( channelId ) );
    }

    /**
     * Get all artifact properties of a channel
     * <p>
     * This method should provide a simple way of simply getting all artifact
     * properties and work around a n+1 select issue when the whole channel is
     * fetched and meta data is required
     * </p>
     *
     * @param channel
     *            the channel
     * @return all meta data
     */
    public Multimap<String, MetaDataEntry> getChannelArtifactProperties ( final ChannelEntity channel )
    {
        final Multimap<String, MetaDataEntry> result = HashMultimap.create ();

        /*
         * Load in two steps, EclipseLink seems to have problems selecting over an abstract type
         */

        loadChannelArtifactMetaData ( channel, ExtractedArtifactPropertyEntity.class, result );
        loadChannelArtifactMetaData ( channel, ProvidedArtifactPropertyEntity.class, result );

        return result;
    }

    private <T extends ArtifactPropertyEntity> void loadChannelArtifactMetaData ( final ChannelEntity channel, final Class<T> clazz, final Multimap<String, MetaDataEntry> result )
    {
        final TypedQuery<Object[]> q = this.em.createQuery ( String.format ( "select aep.artifact.id,aep from %s aep where aep.artifact.channel=:channel", clazz.getName () ), Object[].class );
        q.setParameter ( "channel", channel );

        for ( final Object[] entry : q.getResultList () )
        {
            final String artifactId = (String)entry[0];
            @SuppressWarnings ( "unchecked" )
            final T aep = (T)entry[1];

            final MetaDataEntry mdEntry = new MetaDataEntry ( new MetaKey ( aep.getNamespace (), aep.getKey () ), aep.getValue () );
            result.put ( artifactId, mdEntry );
        }
    }

    protected Set<ArtifactInformation> getArtifacts ( final ChannelEntity channel )
    {
        final Multimap<String, MetaDataEntry> properties = getChannelArtifactProperties ( channel );
        return listArtifacts ( channel, ( ae ) -> convert ( ae, properties ) );
    }

    /**
     * List artifacts with metadata
     *
     * @param channel
     *            the channel to check
     * @return the set of detailed meta data object
     */
    public Set<DetailedArtifactInformation> getDetailedArtifacts ( final ChannelEntity channel )
    {
        final Multimap<String, MetaDataEntry> properties = getChannelArtifactProperties ( channel );
        return listArtifacts ( channel, ( ae ) -> convertDetailed ( ae, properties ) );
    }

    public void recreateVirtualArtifacts ( final ArtifactEntity artifact )
    {
        // delete virtual artifacts

        deleteVirtualChildren ( artifact );

        // recreate

        final ChannelEntity channel = artifact.getChannel ();

        final RegenerateTracker tracker = new RegenerateTracker ();

        try
        {
            this.blobStore.doStreamed ( this.em, artifact, ( file ) -> createVirtualArtifacts ( channel, artifact, file, tracker, false ) );
        }
        catch ( final Exception e )
        {
            throw new RuntimeException ( e );
        }

        this.em.flush ();

        tracker.process ( this );

        // run aggregator after all artifacts have been created
        runChannelAggregators ( channel );
    }

    public void recreateAllVirtualArtifacts ( final ChannelEntity channel )
    {
        final RegenerateTracker tracker = new RegenerateTracker ();

        deleteAllVirtualArtifacts ( channel );
        createAllVirtualArtifacts ( channel, tracker, false );

        tracker.process ( this );

        runChannelAggregators ( channel );
    }

    /**
     * Create all virtual artifacts for a channel
     *
     * @param channel
     *            the channel to process
     * @param runAggregator
     *            whether to run the channel aggregators when an artifact is
     *            created or not
     */
    private void createAllVirtualArtifacts ( final ChannelEntity channel, final RegenerateTracker tracker, final boolean runAggregator )
    {
        scanArtifacts ( channel, ( artifact ) -> this.blobStore.doStreamed ( this.em, artifact, ( file ) -> createVirtualArtifacts ( channel, artifact, file, tracker, runAggregator ) ) );
    }

    /**
     * Delete all channels
     * <p>
     * <em>Note:</em> This call will ignore the lock status of the channels
     * </p>
     */
    public void wipeAllChannels ()
    {
        // we have to do this one by one in order to honor channel locks
        for ( final String channelId : getAllChannelIds () )
        {
            deleteChannel ( channelId, true );
        }
    }

    private List<String> getAllChannelIds ()
    {
        final TypedQuery<String> q = this.em.createQuery ( String.format ( "select c.id from %s c", ChannelEntity.class.getName () ), String.class );
        return q.getResultList ();
    }

    protected void deleteAllCacheEntries ( final ChannelEntity channel )
    {
        final Query q = this.em.createQuery ( String.format ( "DELETE from %s cce where cce.channel=:channel", ChannelCacheEntity.class.getName () ) );
        q.setParameter ( "channel", channel );
        final int result = q.executeUpdate ();

        logger.info ( "Deleted {} cache entries in channel {}", result, channel.getId () );

        this.em.flush ();
    }

    protected void deleteCacheEntries ( final String namespace, final ChannelEntity channel )
    {
        final Query q = this.em.createQuery ( String.format ( "DELETE from %s cce where cce.channel=:channel and cce.namepsace=:ns", ChannelCacheEntity.class.getName () ) );
        q.setParameter ( "channel", channel );
        q.setParameter ( "ns", namespace );
        final int result = q.executeUpdate ();

        logger.info ( "Deleted {} cache entries in channel {} for namespace {}", result, channel.getId (), namespace );

        this.em.flush ();
    }

    public void internalCreateCacheEntry ( final ChannelEntity channel, final String namespace, final String id, final String name, final String mimeType, final IOConsumer<OutputStream> creator ) throws IOException
    {
        final ChannelCacheEntity cce = new ChannelCacheEntity ();

        final ByteArrayOutputStream bos = new ByteArrayOutputStream ();
        creator.accept ( bos );
        bos.close ();
        final byte[] data = bos.toByteArray ();

        cce.setCreationTimestamp ( new Date () );
        cce.setChannel ( channel );
        cce.setNamespace ( namespace );
        cce.setKey ( id );
        cce.setData ( data );
        cce.setSize ( data.length );
        cce.setName ( name );
        cce.setMimeType ( mimeType );

        this.em.persist ( cce );
    }

    public List<CacheEntryInformation> getAllCacheEntries ( final String channelId )
    {
        final ChannelEntity channel = getCheckedChannel ( channelId );

        final TypedQuery<ChannelCacheEntity> q = this.em.createQuery ( String.format ( "SELECT cce from %s cce where cce.channel=:channel", ChannelCacheEntity.class.getName () ), ChannelCacheEntity.class );
        q.setParameter ( "channel", channel );

        final List<ChannelCacheEntity> rl = q.getResultList ();

        final List<CacheEntryInformation> result = new ArrayList<> ( rl.size () );

        for ( final ChannelCacheEntity cce : rl )
        {
            result.add ( new CacheEntryInformationImpl ( new MetaKey ( cce.getNamespace (), cce.getKey () ), cce.getName (), cce.getSize (), cce.getMimeType (), cce.getCreationTimestamp () ) );
        }

        return result;
    }

    public void streamCacheEntry ( final String channelId, final String namespace, final String key, final ThrowingConsumer<CacheEntry> consumer ) throws FileNotFoundException
    {
        if ( consumer == null )
        {
            return;
        }

        final ChannelCacheKey ccKey = new ChannelCacheKey ();
        ccKey.setChannel ( channelId );
        ccKey.setNamespace ( namespace );
        ccKey.setKey ( key );

        final ChannelCacheEntity cce = this.em.find ( ChannelCacheEntity.class, ccKey );

        if ( cce == null )
        {
            throw new FileNotFoundException ( ccKey.toString () );
        }

        try
        {
            final ByteArrayInputStream stream = new ByteArrayInputStream ( cce.getData () );;
            consumer.accept ( new CacheEntryImpl ( stream, cce ) );
        }
        catch ( final Exception e )
        {
            logger.warn ( "Failed to stream cache entry:  " + ccKey, e );
            throw new RuntimeException ( e );
        }
    }

    public ArtifactEntity internalCreateArtifact ( final String channelId, final String name, final Supplier<ArtifactEntity> entityCreator, final InputStream stream, final Map<MetaKey, String> providedMetaData, final boolean external )
    {
        return internalCreateArtifact ( getCheckedChannel ( channelId ), name, entityCreator, stream, providedMetaData, external );
    }

    public ArtifactEntity internalCreateArtifact ( final ChannelEntity channel, final String name, final Supplier<ArtifactEntity> entityCreator, final InputStream stream, final Map<MetaKey, String> providedMetaData, final boolean external )
    {
        try
        {
            final RegenerateTracker tracker = new RegenerateTracker ();
            final ArtifactEntity ae = performStoreArtifact ( channel, name, stream, entityCreator, providedMetaData, tracker, false, external );
            tracker.process ( this );
            runChannelAggregators ( channel );
            return ae;
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

    public ArtifactEntity createAttachedArtifact ( final String parentArtifactId, final String name, final InputStream stream, final Map<MetaKey, String> providedMetaData )
    {
        final ArtifactEntity parentArtifact = getCheckedArtifact ( parentArtifactId );

        testLocked ( parentArtifact.getChannel () );

        if ( parentArtifact instanceof GeneratorArtifactEntity )
        {
            throw new IllegalArgumentException ( String.format ( "Parent Artifact '%s' is a generator artifact", parentArtifact ) );
        }

        if ( ! ( parentArtifact instanceof StoredArtifactEntity ) && ! ( parentArtifact instanceof AttachedArtifactEntity ) )
        {
            throw new IllegalArgumentException ( String.format ( "Parent Artifact '%s' is not a normal stored artifact", parentArtifact ) );
        }

        final ArtifactEntity newArtifact = internalCreateArtifact ( parentArtifact.getChannel ().getId (), name, () -> {
            final AttachedArtifactEntity a = new AttachedArtifactEntity ();
            a.setParent ( parentArtifact );
            parentArtifact.getChildArtifacts ().add ( a );
            return a;
        } , stream, providedMetaData, true );

        return newArtifact;
    }

    public SortedMap<MetaKey, String> getChannelMetaData ( final String channelId )
    {
        final ChannelEntity channel = this.em.find ( ChannelEntity.class, channelId );
        if ( channel == null )
        {
            return null;
        }

        return convertMetaData ( channel );
    }

    public SortedMap<MetaKey, String> getChannelProvidedMetaData ( final String channelId )
    {
        final ChannelEntity channel = this.em.find ( ChannelEntity.class, channelId );
        if ( channel == null )
        {
            return null;
        }

        return convertMetaData ( null, channel.getProvidedProperties () );
    }

    public void clearChannel ( final String channelId )
    {
        final ChannelEntity channel = getCheckedChannel ( channelId );

        testLocked ( channel );

        // processing by "clear" triggers the entity listeners

        channel.getArtifacts ().clear ();
        this.em.persist ( channel );

        runChannelAggregators ( channel );
    }

    private static Set<String> expandDependencies ( final Set<String> aspects )
    {
        final Map<String, ChannelAspectInformation> all = Activator.getChannelAspects ().getAspectInformations ();

        final Set<String> result = new HashSet<> ();
        final TreeSet<String> requested = new TreeSet<> ();
        requested.addAll ( aspects );

        while ( !requested.isEmpty () )
        {
            final String id = requested.pollFirst ();

            if ( result.add ( id ) )
            {
                final ChannelAspectInformation asp = all.get ( id );

                final Set<String> reqs = new HashSet<> ( asp.getRequires () );
                reqs.removeAll ( requested ); // remove all which are already present
                requested.addAll ( reqs ); // add to request list
            }
        }

        return result;
    }

    protected void internalAddAspects ( final EntityManager em, final ChannelEntity channel, final Set<String> aspectFactoryIds ) throws Exception
    {
        logger.debug ( "Adding aspects - channel: {}, aspects: {}", channel.getId (), aspectFactoryIds );

        final Set<String> added = new HashSet<> ();
        for ( final String aspectFactoryId : aspectFactoryIds )
        {
            if ( !channel.getAspects ().containsKey ( aspectFactoryId ) )
            {
                channel.getAspects ().put ( aspectFactoryId, "0.0.0" ); // always start with empty version
                added.add ( aspectFactoryId );
            }
        }

        em.persist ( channel );
        em.flush ();

        reprocessAspects ( channel, added );
    }

    /**
     * Add aspects to a channel
     * <p>
     * <em>Note:</em> This method will <em>not</em> lock the channel for
     * modifications
     * </p>
     *
     * @param channel
     *            the channel to process
     * @param aspects
     *            the aspects to add
     * @param withDependencies
     *            whether to add dependencies or not
     * @throws Exception
     *             if anything goes wrong
     */
    public void addChannelAspects ( final ChannelEntity channel, final Set<String> aspects, final boolean withDependencies ) throws Exception
    {
        testLocked ( channel );

        if ( !withDependencies )
        {
            internalAddAspects ( this.em, channel, aspects );
        }
        else
        {
            internalAddAspects ( this.em, channel, expandDependencies ( aspects ) );
        }
    }

    /**
     * Add aspects to a channel
     * <p>
     * <em>Note:</em> This method will lock the channel for modifications
     * </p>
     *
     * @param channelId
     *            the id of the channel
     * @param aspects
     *            the aspects to add
     * @param withDependencies
     *            whether to add dependencies or not
     */
    public void addChannelAspects ( final String channelId, final Set<String> aspects, final boolean withDependencies )
    {
        // we must lock this before the get call
        this.lockManager.modifyRun ( channelId, () -> {
            addChannelAspects ( getCheckedChannel ( channelId ), aspects, withDependencies );
        } );
    }

    public void deleteChannel ( final String channelId, final boolean ignoreLock )
    {
        this.lockManager.modifyRun ( channelId, () -> {

            final ChannelEntity entity = getCheckedChannel ( channelId );
            if ( !ignoreLock )
            {
                testLocked ( entity );
            }
            this.em.remove ( entity );
            this.em.flush ();
        } );

        // finally remove the channel lock from the cache
        this.lockManager.removeLock ( channelId );
    }
}
