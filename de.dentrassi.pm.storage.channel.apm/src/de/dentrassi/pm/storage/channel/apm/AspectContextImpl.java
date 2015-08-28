package de.dentrassi.pm.storage.channel.apm;

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

    public AspectContextImpl ( final AspectableContext context, final ChannelAspectProcessor processor )
    {
        this.context = context;
        this.processor = processor;
        this.model = context.getAspectModel ();
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

        extractFor ( addedAspects );

        // TODO: run extractors
        // TODO: run virtualizers

        // TODO: flush generators

        // TODO: run aggregators
    }

    public void removeAspects ( final Set<String> aspectIds )
    {
        for ( final String aspectId : aspectIds )
        {
            this.model.remove ( aspectId );
        }

        removeExtractedFor ( aspectIds );

        // TODO: remove virtualized

        // TODO: flush generators

        // TODO: run aggregators
    }

    public void refreshAspects ( final Set<String> aspectIds )
    {
        extractFor ( aspectIds );

        // TODO: remove virtualized
        // TODO: run virtualized

        // TODO: run flush generators

        // TODO: run aggregators
    }

    public ArtifactInformation createArtifact ( final String parentId, final InputStream stream, final String name, final Map<MetaKey, String> providedMetaData )
    {
        final ArtifactInformation result = this.context.createPlainArtifact ( parentId, stream, name, providedMetaData, Collections.singleton ( "stored" ) );

        final Map<MetaKey, String> metaData = extractMetaData ( result, this.model.getAspectIds () );
        this.context.setExtractedMetaData ( result.getId (), metaData );

        // TODO: run virtualizers for artifact

        // TODO: flush generators

        // TODO: run aggregators

        return result;
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
