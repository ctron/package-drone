package de.dentrassi.pm.storage.channel.apm;

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

import com.google.common.io.ByteStreams;

import de.dentrassi.osgi.utils.Exceptions;
import de.dentrassi.pm.aspect.ChannelAspect;
import de.dentrassi.pm.aspect.ChannelAspectProcessor;
import de.dentrassi.pm.aspect.extract.Extractor;
import de.dentrassi.pm.common.ChannelAspectInformation;
import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.common.Severity;
import de.dentrassi.pm.storage.channel.ArtifactInformation;

public class AspectContextImpl
{
    private final AspectableContext context;

    private final ChannelAspectProcessor processor;

    private final AspectMapModel model;

    private final EventProcessor eventProcessor;

    public AspectContextImpl ( final AspectableContext context, final ChannelAspectProcessor processor )
    {
        this.context = context;
        this.processor = processor;
        this.model = context.getAspectModel ();
        this.eventProcessor = new EventProcessor ();
    }

    public SortedMap<String, String> getAspectStates ()
    {
        return this.model.getAspects ();
    }

    public void addAspects ( final Set<String> aspectIds )
    {
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

        // TODO: run virtualizers
        virtualizeFor ( addedAspects );

        // TODO: flush generators

        // TODO: run aggregators
    }

    public void removeAspects ( final Set<String> aspectIds )
    {
        for ( final String aspectId : aspectIds )
        {
            this.model.remove ( aspectId );
        }

        // remove selected extracted meta data

        removeExtractedFor ( aspectIds );
        removeVirtualized ( aspectIds );

        // TODO: remove virtualized

        // TODO: flush generators

        // TODO: run aggregators
    }

    public void refreshAspects ( Set<String> aspectIds )
    {
        if ( aspectIds == null )
        {
            aspectIds = new HashSet<> ( this.model.getAspectIds () );
        }

        // update version map
        for ( final ChannelAspectInformation aspect : this.processor.resolve ( aspectIds ) )
        {
            final String versionString = aspect.getVersion () != null ? aspect.getVersion ().toString () : null;
            this.model.put ( aspect.getFactoryId (), versionString );
        }

        // extract metadata ... well clear itself first

        extractFor ( aspectIds );

        // delete virtualized
        removeVirtualized ( aspectIds );

        // run virtualized
        virtualizeFor ( aspectIds );

        // TODO: run flush generators

        // TODO: run aggregators
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

    private ArtifactInformation internalCreateArtifact ( final String parentId, final InputStream stream, final String name, final Map<MetaKey, String> providedMetaData, final ArtifactType type, final String virtualizerAspectId ) throws IOException
    {
        final Path tmp = Files.createTempFile ( "upload-", null );

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
                result = this.context.createPlainArtifact ( parentId, in, name, providedMetaData, Collections.singleton ( type.getFacetType () ), virtualizerAspectId );
            }

            // extract meta data

            final Map<MetaKey, String> metaData = extractMetaData ( result, this.model.getAspectIds () ); // FIXME: do with tmp file
            result = this.context.setExtractedMetaData ( result.getId (), metaData );

            // TODO: notify generators

            // run virtualizers for artifact
            virtualize ( result, tmp, this.model.getAspectIds () );

            // TODO: flush generators

            // TODO: run aggregators

            return result;
        }
        finally
        {
            Files.deleteIfExists ( tmp );
        }
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
        this.processor.processWithAspect ( aspects, ChannelAspect::getArtifactVirtualizer, ( aspect, virtualizer ) -> {
            final VirtualizerContextImpl ctx = new VirtualizerContextImpl ( aspect.getId (), tmp, artifact, this.context, this::internalCreateArtifact );
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
        int count = 0;
        for ( final String artifactId : artifactIds )
        {
            final boolean result = this.context.deletePlainArtifact ( artifactId );
            if ( result )
            {
                count++;
            }
        }

        // TODO: run flush generators

        // TODO: run aggregators

        return count > 0;
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

            this.processor.processWithAspect ( aspectIds, ChannelAspect::getExtractor, ( aspect, extractor ) -> {
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
