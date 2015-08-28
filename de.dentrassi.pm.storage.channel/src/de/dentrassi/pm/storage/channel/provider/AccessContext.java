package de.dentrassi.pm.storage.channel.provider;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;

import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.common.utils.IOConsumer;
import de.dentrassi.pm.storage.channel.ArtifactInformation;
import de.dentrassi.pm.storage.channel.CacheEntry;
import de.dentrassi.pm.storage.channel.CacheEntryInformation;
import de.dentrassi.pm.storage.channel.ChannelState;

public interface AccessContext
{
    public ChannelState getState ();

    public SortedMap<MetaKey, String> getMetaData ();

    public Map<String, ArtifactInformation> getArtifacts ();

    public boolean stream ( String artifactId, IOConsumer<InputStream> consumer ) throws IOException;

    public default boolean stream ( final ArtifactInformation artifact, final IOConsumer<InputStream> consumer ) throws IOException
    {
        return stream ( artifact.getId (), consumer );
    }

    public default SortedMap<String, String> getAspectStates ()
    {
        return Collections.emptySortedMap ();
    }

    public Map<MetaKey, CacheEntryInformation> getCacheEntries ();

    public boolean streamCacheEntry ( MetaKey key, IOConsumer<CacheEntry> consumer ) throws IOException;
}
