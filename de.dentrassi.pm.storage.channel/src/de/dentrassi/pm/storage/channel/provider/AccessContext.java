package de.dentrassi.pm.storage.channel.provider;

import java.util.Collection;
import java.util.SortedMap;

import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.storage.channel.ArtifactInformation;
import de.dentrassi.pm.storage.channel.ChannelState;

public interface AccessContext
{
    public ChannelState getState ();

    public SortedMap<MetaKey, String> getMetaData ();

    public Collection<ArtifactInformation> getArtifacts ();
}
