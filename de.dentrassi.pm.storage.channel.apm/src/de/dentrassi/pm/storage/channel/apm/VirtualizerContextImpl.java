package de.dentrassi.pm.storage.channel.apm;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Map;

import de.dentrassi.osgi.utils.Exceptions;
import de.dentrassi.pm.aspect.virtual.Virtualizer;
import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.storage.channel.ArtifactInformation;
import de.dentrassi.pm.storage.channel.apm.AspectContextImpl.ArtifactCreator;

public class VirtualizerContextImpl implements Virtualizer.Context
{
    private final String virtualizerAspectId;

    private final Path tmp;

    private final ArtifactInformation artifact;

    private final AspectableContext context;

    private final ArtifactCreator creator;

    public VirtualizerContextImpl ( final String virtualizerAspectId, final Path tmp, final ArtifactInformation artifact, final AspectableContext context, final ArtifactCreator creator )
    {
        this.virtualizerAspectId = virtualizerAspectId;

        this.tmp = tmp;
        this.artifact = artifact;
        this.context = context;
        this.creator = creator;
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
        Exceptions.wrapException ( () -> this.creator.internalCreateArtifact ( this.artifact.getId (), stream, name, providedMetaData, ArtifactType.VIRTUAL, this.virtualizerAspectId ) );
    }
}
