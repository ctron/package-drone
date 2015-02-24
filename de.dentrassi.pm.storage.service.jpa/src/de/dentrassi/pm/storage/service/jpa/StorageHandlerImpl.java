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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
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

import com.google.common.io.ByteStreams;

import de.dentrassi.pm.aspect.ChannelAspect;
import de.dentrassi.pm.aspect.ChannelAspectProcessor;
import de.dentrassi.pm.aspect.aggregate.AggregationContext;
import de.dentrassi.pm.aspect.listener.ChannelListener;
import de.dentrassi.pm.aspect.listener.PostAddContext;
import de.dentrassi.pm.aspect.virtual.Virtualizer;
import de.dentrassi.pm.common.ArtifactInformation;
import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.common.event.AddedEvent;
import de.dentrassi.pm.common.event.RemovedEvent;
import de.dentrassi.pm.generator.GenerationContext;
import de.dentrassi.pm.generator.GeneratorProcessor;
import de.dentrassi.pm.storage.StorageAccessor;
import de.dentrassi.pm.storage.jpa.ArtifactEntity;
import de.dentrassi.pm.storage.jpa.ArtifactEntity_;
import de.dentrassi.pm.storage.jpa.AttachedArtifactEntity;
import de.dentrassi.pm.storage.jpa.ChannelEntity;
import de.dentrassi.pm.storage.jpa.GeneratedArtifactEntity;
import de.dentrassi.pm.storage.jpa.GeneratorArtifactEntity;
import de.dentrassi.pm.storage.jpa.StoredArtifactEntity;
import de.dentrassi.pm.storage.jpa.VirtualArtifactEntity;

public class StorageHandlerImpl implements StorageAccessor, StreamServiceHelper
{

    private final static Logger logger = LoggerFactory.getLogger ( StorageHandlerImpl.class );

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

            return convert ( this.em.find ( ArtifactEntity.class, artifactId ) );
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
                performStoreArtifact ( this.channel, name, stream, this.em, this.entitySupplier, providedMetaData, this.tracker, this.runAggregator, false );
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

        @Override
        public StorageAccessor getStorage ()
        {
            return StorageHandlerImpl.this;
        }
    }

    private final EntityManager em;

    private final GeneratorProcessor generatorProcessor;

    private final ChannelAspectProcessor channelAspectProcessor = Activator.getChannelAspects ();

    private final LockManager<String> lockManager;

    public StorageHandlerImpl ( final EntityManager em, final GeneratorProcessor generatorProcessor, final LockManager<String> lockManager )
    {
        this.em = em;
        this.generatorProcessor = generatorProcessor;
        this.lockManager = lockManager;
    }

    protected ChannelEntity getCheckedChannel ( final String channelId )
    {
        final ChannelEntity channel = this.em.find ( ChannelEntity.class, channelId );
        if ( channel == null )
        {
            throw new IllegalArgumentException ( String.format ( "Channel %s unknown", channelId ) );
        }
        return channel;
    }

    protected ArtifactEntity getCheckedArtifact ( final String artifactId )
    {
        final ArtifactEntity artifact = this.em.find ( ArtifactEntity.class, artifactId );
        if ( artifact == null )
        {
            throw new IllegalArgumentException ( String.format ( "Artifact %s unknown", artifactId ) );
        }
        return artifact;
    }

    @Override
    public void updateChannel ( final String channelId, final String name )
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
        this.lockManager.modifyRun ( ae.getChannel ().getId (), ( ) -> {
            doStreamed ( this.em, ae, ( file ) -> {
                // first clear old generated artifacts

                deleteGeneratedChildren ( ae );
                this.em.flush ();

                generateArtifact ( ae.getChannel (), ae, file, runAggregator );
                this.em.flush ();
            } );
        } );
    }

    protected void deleteGeneratedChildren ( final GeneratorArtifactEntity ae )
    {
        final Query q = this.em.createQuery ( String.format ( "DELETE from %s ga where ga.parent=:parent", GeneratedArtifactEntity.class.getSimpleName () ) );
        q.setParameter ( "parent", ae );
        q.executeUpdate ();
    }

    protected void deleteVirtualChildren ( final ArtifactEntity artifact )
    {
        final Query q = this.em.createQuery ( String.format ( "DELETE from %s va where va.parent=:parent", VirtualArtifactEntity.class.getSimpleName () ) );
        q.setParameter ( "parent", artifact );
        q.executeUpdate ();
    }

    public void generateArtifact ( final ChannelEntity channel, final GeneratorArtifactEntity ae, final Path file, final boolean runAggregator ) throws Exception
    {
        final String generatorId = ae.getGeneratorId ();

        final RegenerateTracker tracker = new RegenerateTracker ( this );

        final ArtifactContextImpl ctx = createGeneratedContext ( tracker, this.em, channel, ae, file, runAggregator );
        this.generatorProcessor.process ( generatorId, ctx );

        tracker.process ( runAggregator );
    }

    private ArtifactContextImpl createGeneratedContext ( final RegenerateTracker tracker, final EntityManager em, final ChannelEntity channel, final ArtifactEntity artifact, final Path file, final boolean runAggregator )
    {
        return new ArtifactContextImpl ( channel, tracker, runAggregator, file, ( ) -> convert ( artifact ), em, ( ) -> {
            final GeneratedArtifactEntity ge = new GeneratedArtifactEntity ();
            ge.setParent ( artifact );
            return ge;
        } );
    }

    public ArtifactEntity performStoreArtifact ( final ChannelEntity channel, final String name, final InputStream stream, final EntityManager em, final Supplier<ArtifactEntity> entityCreator, final Map<MetaKey, String> providedMetaData, final RegenerateTracker tracker, final boolean runAggregator, final boolean external ) throws Exception
    {
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

            final SortedMap<MetaKey, String> metadata = extractMetaData ( em, channel, file );

            return this.lockManager.modifyCall ( channel.getId (), ( ) -> {

                ArtifactEntity ae;
                try ( BufferedInputStream in = new BufferedInputStream ( new FileInputStream ( file.toFile () ) ) )
                {
                    ae = storeBlob ( entityCreator, em, channel, name, in, metadata, providedMetaData );
                }

                if ( ae instanceof GeneratorArtifactEntity )
                {
                    generateArtifact ( channel, (GeneratorArtifactEntity)ae, file, runAggregator );
                }

                final AddedEvent event = new AddedEvent ( ae.getId (), metadata );
                runChannelTriggers ( tracker, channel, listener -> listener.artifactAdded ( createPostAddContext ( channel.getId () ) ), event );

                createVirtualArtifacts ( channel, ae, file, tracker, runAggregator );

                // runGeneratorTriggers ( channel, new AddedEvent ( ae.getId (), metadata ) );

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
        Activator.getChannelAspects ().process ( channel.getAspects (), ChannelAspect::getChannelListener, ( t ) -> {
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

        Activator.getChannelAspects ().process ( channel.getAspects (), ChannelAspect::getExtractor, extractor -> {
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

        Activator.getChannelAspects ().processWithAspect ( channel.getAspects (), ChannelAspect::getArtifactVirtualizer, ( aspect, virtualizer ) -> virtualizer.virtualize ( createArtifactContext ( this.em, channel, artifact, file, aspect.getId (), tracker, runAggregator ) ) );
    }

    private ArtifactContextImpl createArtifactContext ( final EntityManager em, final ChannelEntity channel, final ArtifactEntity artifact, final Path file, final String namespace, final RegenerateTracker tracker, final boolean runAggregator )
    {
        logger.debug ( "Creating virtual artifact context for: {}", namespace );

        return new ArtifactContextImpl ( channel, tracker, runAggregator, file, ( ) -> convert ( artifact ), em, ( ) -> {
            final VirtualArtifactEntity ve = new VirtualArtifactEntity ();
            ve.setParent ( artifact );
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

        final RegenerateTracker tracker = new RegenerateTracker ( this );
        runGeneratorTriggers ( tracker, channel, new RemovedEvent ( ae.getId (), md ) );
        tracker.process ( false );

        // now run the channel aggregator
        runChannelAggregators ( channel );

        return convert ( ae );
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

        final Map<MetaKey, String> metadata = new HashMap<> ();

        final AggregationContext context = createAggregationContext ( channel );

        this.channelAspectProcessor.process ( channel.getAspects (), ChannelAspect::getChannelAggregator, aggregator -> {
            try
            {
                final Map<String, String> md = aggregator.aggregateMetaData ( context );
                convertMetaDataFromExtractor ( metadata, aggregator.getId (), md );
            }
            catch ( final Exception e )
            {
                throw new RuntimeException ( String.format ( "Failed to run channel aggregator: %s", aggregator.getId () ), e );
            }
        } );

        // clear
        this.lockManager.modifyRun ( channel.getId (), ( ) -> {
            channel.getExtractedProperties ().clear ();

            this.em.persist ( channel );
            this.em.flush ();

            // set

            Helper.convertExtractedProperties ( metadata, channel, channel.getExtractedProperties () );

            this.em.persist ( channel );
            this.em.flush ();
        } );
    }

    private AggregationContext createAggregationContext ( final ChannelEntity channel )
    {
        final Collection<ArtifactInformation> artifacts = getArtifacts ( channel );
        final SortedMap<MetaKey, String> metaData = convertMetaData ( null, channel.getProvidedProperties () );

        return new AggregationContext () {

            @Override
            public Collection<ArtifactInformation> getArtifacts ()
            {
                return artifacts;
            }

            @Override
            public Map<MetaKey, String> getChannelMetaData ()
            {
                return metaData;
            }
        };
    }

    public void runChannelAggregators ( final String channelId )
    {
        runChannelAggregators ( getCheckedChannel ( channelId ) );
    }

    protected void reprocessAspect ( final ChannelEntity channel, final String aspectFactoryId ) throws Exception
    {
        logger.info ( "Reprocessing aspect - channelId: {}, aspect: {}", channel.getId (), aspectFactoryId );

        final RegenerateTracker tracker = new RegenerateTracker ( this );

        // first delete all virtual artifacts

        deleteAllVirtualArtifacts ( channel );

        // process new meta data

        for ( final ArtifactEntity ae : channel.getArtifacts () )
        {
            if ( ae instanceof GeneratorArtifactEntity )
            {
                continue;
            }

            logger.debug ( "Reprocessing artifact - {}", ae.getId () );

            doStreamed ( this.em, ae, ( file ) -> {
                // generate metadata for new factory

                final Map<MetaKey, String> metadata = new HashMap<> ();
                final List<String> list = Arrays.asList ( aspectFactoryId );
                this.channelAspectProcessor.process ( list, ChannelAspect::getExtractor, extractor -> {
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

        tracker.process ( false );

        runChannelAggregators ( channel );
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

    public <T extends Comparable<? super T>> Set<T> listArtifacts ( final String channelId, final Function<ArtifactEntity, T> mapper )
    {
        return listArtifacts ( getCheckedChannel ( channelId ), mapper );
    }

    @Override
    public Set<ArtifactInformation> getArtifacts ( final String channelId )
    {
        return listArtifacts ( getCheckedChannel ( channelId ), ( ae ) -> convert ( ae ) );
    }

    protected Set<ArtifactInformation> getArtifacts ( final ChannelEntity ce )
    {
        return listArtifacts ( ce, ( ae ) -> convert ( ae ) );
    }

    public void recreateVirtualArtifacts ( final ArtifactEntity artifact )
    {
        // delete virtual artifacts

        deleteVirtualChildren ( artifact );

        // recreate

        final ChannelEntity channel = artifact.getChannel ();

        final RegenerateTracker tracker = new RegenerateTracker ( this );

        try
        {
            doStreamed ( this.em, artifact, ( file ) -> createVirtualArtifacts ( channel, artifact, file, tracker, false ) );
        }
        catch ( final Exception e )
        {
            throw new RuntimeException ( e );
        }

        this.em.flush ();

        tracker.process ( false );

        // run aggregator after all artifacts have been created
        runChannelAggregators ( channel );
    }

    public void recreateAllVirtualArtifacts ( final ChannelEntity channel )
    {
        final RegenerateTracker tracker = new RegenerateTracker ( this );

        deleteAllVirtualArtifacts ( channel );
        createAllVirtualArtifacts ( channel, tracker, false );

        tracker.process ( false );

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
        scanArtifacts ( channel, ( artifact ) -> doStreamed ( this.em, artifact, ( file ) -> createVirtualArtifacts ( channel, artifact, file, tracker, runAggregator ) ) );
    }

    private void deleteAllVirtualArtifacts ( final ChannelEntity channel )
    {
        final Query q = this.em.createQuery ( String.format ( "DELETE from %s va where va.channel=:channel", VirtualArtifactEntity.class.getSimpleName () ) );
        q.setParameter ( "channel", channel );
        final int result = q.executeUpdate ();

        logger.info ( "Deleted {} artifacts in channel {}", result, channel.getId () );

        this.em.flush ();
    }

    public ArtifactEntity internalCreateArtifact ( final String channelId, final String name, final Supplier<ArtifactEntity> entityCreator, final InputStream stream, final Map<MetaKey, String> providedMetaData, final boolean external )
    {
        try
        {
            final ChannelEntity channel = getCheckedChannel ( channelId );
            final RegenerateTracker tracker = new RegenerateTracker ( this );
            final ArtifactEntity ae = performStoreArtifact ( channel, name, stream, this.em, entityCreator, providedMetaData, tracker, false, external );
            tracker.process ( false );
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

        final ArtifactEntity newArtifact = internalCreateArtifact ( parentArtifact.getChannel ().getId (), name, ( ) -> {
            final AttachedArtifactEntity a = new AttachedArtifactEntity ();
            a.setParent ( parentArtifact );
            return a;
        }, stream, providedMetaData, true );

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

        final Query q = this.em.createQuery ( String.format ( "DELETE from %s ae where ae.channel.id=:channelId", ArtifactEntity.class.getName () ) );
        q.setParameter ( "channelId", channelId );
        q.executeUpdate ();

        runChannelAggregators ( channel );
    }

}
