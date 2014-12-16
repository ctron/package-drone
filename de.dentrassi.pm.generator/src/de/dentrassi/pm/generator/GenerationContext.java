package de.dentrassi.pm.generator;

import de.dentrassi.pm.common.ArtifactContext;
import de.dentrassi.pm.storage.StorageAccessor;

public interface GenerationContext extends ArtifactContext
{
    public StorageAccessor getStorage ();
}
