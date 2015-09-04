package de.dentrassi.pm.storage.channel.apm.aspect;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.ByteStreams;

import de.dentrassi.osgi.profiler.Profile;
import de.dentrassi.osgi.utils.Exceptions;
import de.dentrassi.pm.aspect.ChannelAspect;
import de.dentrassi.pm.aspect.ChannelAspectProcessor;
import de.dentrassi.pm.aspect.aggregate.AggregationContext;
import de.dentrassi.pm.aspect.extract.Extractor;
import de.dentrassi.pm.common.ChannelAspectInformation;
import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.common.Severity;
import de.dentrassi.pm.common.utils.IOConsumer;
import de.dentrassi.pm.storage.channel.ArtifactInformation;
import de.dentrassi.pm.storage.channel.ChannelDetails;
import de.dentrassi.pm.storage.channel.ChannelService.ArtifactReceiver;
import de.dentrassi.pm.storage.channel.apm.internal.Activator;

public class AspectContextImpl
{
    private final static Logger logger = LoggerFactory.getLogger ( AspectContextImpl.class );

    private final AspectableContext context;

    private final ChannelAspectProcessor processor;

    private final AspectMapModel model;

    private final EventProcessor eventProcessor;

    private final Guard aggregation;

    public AspectContextImpl ( final AspectableContext context, final ChannelAspectProcessor processor )
    {
        this.context = context;
        this.processor = processor;
        this.model = context.getAspectModel ();
        this.eventProcessor = new EventProcessor ();

        this.aggregation = new Guard ( this::runAggregators );
    }

    public SortedMap<String, String> getAspectStates ()
    {
        return this.model.getAspects ();
    }

    protected void runAggregators ()
    {
        logger.debug ( "Running aggregators" );
        Profile.run ( this, "runAggregators", () -> {

            final Map<MetaKey, String> metaData = new HashMap<> ();

            this.processor.process ( this.model.getAspectIds (), ChannelAspect::getChannelAggregator, ( aspect, aggregator ) -> {

                final AggregationContext ctx = new AggregationContextImpl ( this.context, aspect.getId (), this.context.getChannelId (), this.context::getChannelDetails );

                final Map<String, String> result = Exceptions.wrapException ( () -> aggregator.aggregateMetaData ( ctx ) );

                mergeNamespaceMetaData ( aspect, result, metaData );
            } );

            this.context.setExtractedMetaData ( metaData );
        } );
    }

    public void addAspects ( final Set<String> aspectIds )
    {
        this.aggregation.guarded ( () -> {
            final Set<String> addedAspects = new HashSet<> ();
            for ( final ChannelAspectInformation aspect : this.processor.resolve ( aspectIds ) )
            {
                final String versionString = aspect.getVersion () != null ? aspect.getVersion ().toString () : null;

                if ( !this.model.getAspectIds ().contains ( aspect.getFactoryId () ) )
                {
                    this.model.put ( aspect.getFactoryId (), versionString );
                    addedAspects.add ( aspect.getFactoryId () );
                }
            }

            // run extractors

            extractFor ( addedAspects );

            // run virtualizers

            virtualizeFor ( addedAspects );

            // TODO: flush generators

            // aggregators run after with guard
        } );
    }

    public void removeAspects ( final Set<String> aspectIds )
    {
        this.aggregation.guarded ( () -> {
            for ( final String aspectId : aspectIds )
            {
                this.model.remove ( aspectId );
            }

            // remove selected extracted meta data

            removeExtractedFor ( aspectIds );

            // remove virtualized

            removeVirtualized ( aspectIds );

            // TODO: flush generators

            // aggregators run after with guard
        } );
    }

    public void refreshAspects ( final Set<String> aspectIds )
    {
        Set<String> effectiveAspectIds;
        if ( aspectIds == null )
        {
            effectiveAspectIds = new HashSet<> ( this.model.getAspectIds () );
        }
        else
        {
            effectiveAspectIds = aspectIds;
        }

        this.aggregation.guarded ( () -> {

            // update version map
            for ( final ChannelAspectInformation aspect : this.processor.resolve ( effectiveAspectIds ) )
            {
                final String versionString = aspect.getVersion () != null ? aspect.getVersion ().toString () : null;
                this.model.put ( aspect.getFactoryId (), versionString );
            }

            // extract metadata ... well clear itself first

            extractFor ( effectiveAspectIds );

            // delete virtualized
            removeVirtualized ( effectiveAspectIds );

            // run virtualized
            virtualizeFor ( effectiveAspectIds );

            // TODO: run flush generators

            // aggregators run after with guard
        } );
    }

    public static class AggregationContextImpl implements AggregationContext
    {
        private final AspectableContext ctx;

        private final String aspectId;

        private final String channelId;

        private final Supplier<ChannelDetails> details;

        public AggregationContextImpl ( final AspectableContext ctx, final String aspectId, final String channelId, final Supplier<ChannelDetails> details )
        {
            this.ctx = ctx;
            this.aspectId = aspectId;
            this.channelId = channelId;

            this.details = details;
        }

        @Override
        public void validationMessage ( final Severity severity, final String message, final Set<String> artifactIds )
        {
            // FIXME: implement
        }

        @Override
        public Collection<ArtifactInformation> getArtifacts ()
        {
            return Collections.unmodifiableCollection ( this.ctx.getArtifacts ().values () );
        }

        @Override
        public String getChannelId ()
        {
            return this.channelId;
        }

        @Override
        public String getChannelDescription ()
        {
            return this.details.get ().getDescription ();
        }

        @Override
        public Map<MetaKey, String> getChannelMetaData ()
        {
            return Collections.unmodifiableMap ( this.ctx.getChannelProvidedMetaData () );
        }

        @Override
        public void createCacheEntry ( final String id, final String name, final String mimeType, final IOConsumer<OutputStream> creator ) throws IOException
        {
            this.ctx.createCacheEntry ( new MetaKey ( this.aspectId, id ), name, mimeType, creator );
        }

        @Override
        public boolean streamArtifact ( final String artifactId, final ArtifactReceiver receiver ) throws IOException
        {
            final ArtifactInformation artifact = this.ctx.getArtifacts ().get ( artifactId );
            if ( artifact == null )
            {
                return false;
            }

            return this.ctx.stream ( artifactId, stream -> receiver.consume ( artifact, stream ) );
        }

        @Override
        public boolean streamArtifact ( final String artifactId, final IOConsumer<InputStream> consumer ) throws IOException
        {
            return this.ctx.stream ( artifactId, consumer );
        }
    }

    @FunctionalInterface
    public interface ArtifactCreator
    {
        public ArtifactInformation internalCreateArtifact ( final String parentId, final InputStream stream, final String name, final Map<MetaKey, String> providedMetaData, final ArtifactType type, String virtualizerAspectId ) throws IOException;
    }

    public ArtifactInformation createArtifact ( final String parentId, final InputStream stream, final String name, final Map<MetaKey, String> providedMetaData ) throws IOException
    {
        return internalCreateArtifact ( parentId, stream, name, providedMetaData, ArtifactType.STORED, null );
    }

    public ArtifactInformation createGeneratorArtifact ( final String generatorId, final InputStream stream, final String name, final Map<MetaKey, String> providedMetaData ) throws IOException
    {
        return this.aggregation.guarded ( () -> {
            final ArtifactInformation result = internalCreateArtifact ( null, stream, name, providedMetaData, ArtifactType.GENERATOR, generatorId );

            // run generator

            generate ( result );

            // TODO: run flush generators

            // aggregators run after with guard
            return result;
        } );
    }

    public void regenerate ( final String artifactId )
    {
        this.aggregation.guarded ( () -> {
            final ArtifactInformation artifact = this.context.getArtifacts ().get ( artifactId );

            if ( artifact == null )
            {
                throw new IllegalStateException ( String.format ( "Unable to find artifact '%s'", artifactId ) );
            }

            deleteGenerated ( artifact );
            generate ( artifact );

            // TODO: run flush generators

            // aggregators run after with guard
        } );
    }

    private void deleteGenerated ( final ArtifactInformation generator )
    {
        final Set<String> deletions = new HashSet<> ( 1 );

        for ( final String childId : generator.getChildIds () )
        {
            final ArtifactInformation child = this.context.getArtifacts ().get ( childId );
            if ( child == null )
            {
                continue;
            }

            if ( !child.is ( "generated" ) )
            {
                continue;
            }

            deletions.add ( childId );
        }

        deleteArtifacts ( deletions );
    }

    private void generate ( final ArtifactInformation artifact )
    {
        // run generator

        doStreamed ( artifact.getId (), file -> {
            final String generatorId = artifact.getVirtualizerAspectId ();
            final VirtualizerContextImpl ctx = new VirtualizerContextImpl ( generatorId, file, artifact, this.context, this::internalCreateArtifact, ArtifactType.GENERATED );

            Exceptions.wrapException ( () -> Activator.getGeneratorProcessor ().process ( artifact.getVirtualizerAspectId (), ctx ) );
        } );
    }

    private ArtifactInformation internalCreateArtifact ( final String parentId, final InputStream stream, final String name, final Map<MetaKey, String> providedMetaData, final ArtifactType type, final String virtualizerAspectId ) throws IOException
    {
        final Path tmp = Files.createTempFile ( "upload-", null );

        return this.aggregation.guarded ( () -> {
            try
            {
                // spool out to tmp file

                try ( OutputStream out = new BufferedOutputStream ( Files.newOutputStream ( tmp ) ) )
                {
                    ByteStreams.copy ( stream, out );
                }

                // check veto

                if ( checkVetoAdd ( name, tmp, type.isExternal () ) )
                {
                    return null;
                }

                // store artifact

                ArtifactInformation result;
                try ( InputStream in = new BufferedInputStream ( Files.newInputStream ( tmp ) ) )
                {
                    result = this.context.createPlainArtifact ( parentId, in, name, providedMetaData, type.getFacetTypes (), virtualizerAspectId );
                }

                // extract meta data

                final Map<MetaKey, String> metaData = extractMetaData ( result, this.model.getAspectIds () ); // FIXME: do with tmp file
                result = this.context.setExtractedMetaData ( result.getId (), metaData );

                // TODO: notify generators

                // run virtualizers for artifact

                virtualize ( result, tmp, this.model.getAspectIds () );

                // TODO: flush generators

                // aggregators run after with guard
                return result;
            }
            finally
            {
                Files.deleteIfExists ( tmp );
            }
        } );
    }

    private void virtualizeFor ( final Set<String> aspects )
    {
        // we need to iterate over an array

        for ( final ArtifactInformation artifact : this.context.getArtifacts ().values ().toArray ( new ArtifactInformation[this.context.getArtifacts ().size ()] ) )
        {
            doStreamed ( artifact.getId (), tmp -> {
                virtualize ( artifact, tmp, aspects );
            } );
        }
    }

    private void removeVirtualized ( final Set<String> aspectIds )
    {
        final Set<String> artifacts = new HashSet<> ();

        for ( final ArtifactInformation artifact : this.context.getArtifacts ().values ().toArray ( new ArtifactInformation[this.context.getArtifacts ().size ()] ) )
        {
            final String virtualizer = artifact.getVirtualizerAspectId ();
            if ( aspectIds.contains ( virtualizer ) )
            {
                artifacts.add ( artifact.getId () );
            }
        }

        deleteArtifacts ( artifacts );
    }

    private void virtualize ( final ArtifactInformation artifact, final Path tmp, final Collection<String> aspects )
    {
        this.processor.process ( aspects, ChannelAspect::getArtifactVirtualizer, ( aspect, virtualizer ) -> {
            final VirtualizerContextImpl ctx = new VirtualizerContextImpl ( aspect.getId (), tmp, artifact, this.context, this::internalCreateArtifact, ArtifactType.VIRTUAL );
            virtualizer.virtualize ( ctx );
        } );
    }

    private boolean checkVetoAdd ( final String name, final Path file, final boolean external )
    {
        final PreAddContextImpl ctx = new PreAddContextImpl ( name, file, external );
        this.processor.process ( this.model.getAspectIds (), ChannelAspect::getChannelListener, listener -> {
            Exceptions.wrapException ( () -> listener.artifactPreAdd ( ctx ) );
        } );
        return ctx.isVeto ();
    }

    public boolean deleteArtifacts ( final Set<String> artifactIds )
    {
        return this.aggregation.guarded ( () -> {
            int count = 0;
            for ( final String artifactId : artifactIds )
            {
                final boolean result = internalDeleteArtifact ( artifactId );
                if ( result )
                {
                    count++;
                }
            }

            // TODO: run flush generators

            // aggregators run after with guard

            return count > 0;
        } );
    }

    private boolean internalDeleteArtifact ( final String artifactId )
    {
        final boolean result = this.context.deletePlainArtifact ( artifactId );

        if ( result )
        {
            // FIXME: run trigger
        }

        return result;
    }

    private void removeNamespaces ( final Set<String> aspectIds, final Map<MetaKey, String> newMetaData )
    {
        final Iterator<Map.Entry<MetaKey, String>> i = newMetaData.entrySet ().iterator ();
        while ( i.hasNext () )
        {
            final Entry<MetaKey, String> entry = i.next ();
            if ( aspectIds.contains ( entry.getKey ().getNamespace () ) )
            {
                i.remove ();
            }
        }
    }

    private void removeExtractedFor ( final Set<String> aspectIds )
    {
        for ( final ArtifactInformation art : this.context.getArtifacts ().values ().toArray ( new ArtifactInformation[this.context.getArtifacts ().size ()] ) )
        {
            final Map<MetaKey, String> newMetaData = new HashMap<> ( art.getExtractedMetaData () );

            // remove all meta keys which we want to update
            removeNamespaces ( aspectIds, newMetaData );

            this.context.setExtractedMetaData ( art.getId (), newMetaData );
        }
    }

    private void extractFor ( final Set<String> aspectIds )
    {
        // we need to iterate over an array

        for ( final ArtifactInformation art : this.context.getArtifacts ().values ().toArray ( new ArtifactInformation[this.context.getArtifacts ().size ()] ) )
        {
            final Map<MetaKey, String> updatedMetaData = extractMetaData ( art, aspectIds );
            final Map<MetaKey, String> newMetaData = new HashMap<> ( art.getExtractedMetaData () );

            // remove all meta keys which we want to update
            removeNamespaces ( aspectIds, newMetaData );

            // insert new data
            newMetaData.putAll ( updatedMetaData );

            this.context.setExtractedMetaData ( art.getId (), newMetaData );
        }
    }

    private Map<MetaKey, String> extractMetaData ( final ArtifactInformation artifact, final Collection<String> aspectIds )
    {
        final Map<MetaKey, String> result = new HashMap<> ();

        doStreamed ( artifact.getId (), path -> {

            this.processor.process ( aspectIds, ChannelAspect::getExtractor, ( aspect, extractor ) -> {
                try
                {
                    extractMetaData ( artifact, result, path, aspect, extractor );
                }
                catch ( final Exception e )
                {
                    throw new RuntimeException ( e );
                }
            } );
        } );

        return result;
    }

    private void extractMetaData ( final ArtifactInformation artifact, final Map<MetaKey, String> result, final Path path, final ChannelAspect aspect, final Extractor extractor ) throws Exception
    {
        final Map<String, String> md = new HashMap<> ();
        extractor.extractMetaData ( new Extractor.Context () {

            @Override
            public void validationMessage ( final Severity severity, final String message )
            {
                // FIXME: implement
            }

            @Override
            public String getName ()
            {
                return artifact.getName ();
            }

            @Override
            public Path getPath ()
            {
                return path;
            }

            @Override
            public Instant getCreationTimestamp ()
            {
                return artifact.getCreationInstant ();
            }
        }, md );

        // insert into metakey map

        mergeNamespaceMetaData ( aspect, md, result );
    }

    private void mergeNamespaceMetaData ( final ChannelAspect aspect, final Map<String, String> md, final Map<MetaKey, String> result )
    {
        for ( final Map.Entry<String, String> entry : md.entrySet () )
        {
            result.put ( new MetaKey ( aspect.getId (), entry.getKey () ), entry.getValue () );
        }
    }

    private void doStreamed ( final String artifactId, final Consumer<Path> consumer )
    {
        try
        {
            final boolean result = this.context.stream ( artifactId, stream -> {
                final Path tmp = Files.createTempFile ( "blob-", null );
                try
                {
                    try ( OutputStream os = new BufferedOutputStream ( Files.newOutputStream ( tmp ) ) )
                    {
                        ByteStreams.copy ( stream, os );
                    }

                    consumer.accept ( tmp );
                }
                finally
                {
                    Files.deleteIfExists ( tmp );
                }
            } );

            if ( !result )
            {
                throw new IllegalStateException ( "Unable to stream blob for: " + artifactId );
            }

        }
        catch ( final IOException e )
        {
            throw new RuntimeException ( "Failed to stream blob", e );
        }

    }

}
