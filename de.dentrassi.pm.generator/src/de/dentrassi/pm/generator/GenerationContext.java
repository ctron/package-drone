package de.dentrassi.pm.generator;

import de.dentrassi.pm.common.ArtifactContext;
import de.dentrassi.pm.storage.Channel;

public interface GenerationContext extends ArtifactContext
{
    public Channel getChannel ();
}
