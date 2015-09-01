package de.dentrassi.pm.generator;

import java.util.Collection;

import de.dentrassi.pm.storage.channel.ArtifactContext;
import de.dentrassi.pm.storage.channel.ArtifactInformation;

public interface GenerationContext extends ArtifactContext
{
    public Collection<ArtifactInformation> getChannelArtifacts ();
}
