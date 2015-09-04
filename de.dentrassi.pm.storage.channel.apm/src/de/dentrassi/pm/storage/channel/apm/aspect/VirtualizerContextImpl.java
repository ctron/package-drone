package de.dentrassi.pm.storage.channel.apm.aspect;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;

import de.dentrassi.osgi.utils.Exceptions;
import de.dentrassi.pm.aspect.virtual.Virtualizer;
import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.generator.GenerationContext;
import de.dentrassi.pm.storage.channel.ArtifactInformation;
import de.dentrassi.pm.storage.channel.apm.aspect.AspectContextImpl.ArtifactCreator;

public class VirtualizerContextImpl implements Virtualizer.Context, GenerationContext
{
    private final String virtualizerAspectId;

    private final Path tmp;

    private final ArtifactInformation artifact;

    private final AspectableContext context;

    private final ArtifactCreator creator;

    private final ArtifactType type;

    public VirtualizerContextImpl ( final String virtualizerAspectId, final Path tmp, final ArtifactInformation artifact, final AspectableContext context, final ArtifactCreator creator, final ArtifactType type )
    {
        this.virtualizerAspectId = virtualizerAspectId;

        this.tmp = tmp;
        this.artifact = artifact;
        this.context = context;
        this.creator = creator;

        this.type = type;
    }

    @Override
    public Map<MetaKey, String> getProvidedChannelMetaData ()
    {
        return this.context.getChannelProvidedMetaData ();
    }

    @Override
    public ArtifactInformation getOtherArtifactInformation ( final String artifactId )
    {
        return this.context.getArtifacts ().get ( artifactId );
    }

    @Override
    public Collection<ArtifactInformation> getChannelArtifacts ()
    {
        return this.context.getArtifacts ().values ();
    }

    @Override
    public Path getFile ()
    {
        return this.tmp;
    }

    @Override
    public ArtifactInformation getArtifactInformation ()
    {
        return this.artifact;
    }

    @Override
    public void createVirtualArtifact ( final String name, final InputStream stream, final Map<MetaKey, String> providedMetaData )
    {
        Exceptions.wrapException ( () -> this.creator.internalCreateArtifact ( this.artifact.getId (), stream, name, providedMetaData, this.type, this.virtualizerAspectId ) );
    }
}
