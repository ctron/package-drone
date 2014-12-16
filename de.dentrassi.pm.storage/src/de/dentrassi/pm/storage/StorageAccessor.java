package de.dentrassi.pm.storage;

import java.util.Set;

import de.dentrassi.pm.common.ArtifactInformation;

public interface StorageAccessor
{
    public void updateChannel ( String channelId, String name );

    public void regenerateAll ( String channelId );

    public Set<ArtifactInformation> getArtifacts ( String channelId );
}
