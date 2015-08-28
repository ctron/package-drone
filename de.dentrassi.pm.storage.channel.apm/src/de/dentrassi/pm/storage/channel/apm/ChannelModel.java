package de.dentrassi.pm.storage.channel.apm;

import static java.util.stream.Collectors.toMap;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.storage.channel.ArtifactInformation;

public class ChannelModel
{
    private String description;

    private boolean locked;

    private Map<MetaKey, String> metaData;

    private final Map<String, ArtifactModel> artifacts;

    private final Map<MetaKey, CacheEntryModel> cacheEntries;

    private final AspectMapModel aspects;

    public ChannelModel ()
    {
        this.metaData = new HashMap<> ();
        this.artifacts = new HashMap<> ();
        this.cacheEntries = new HashMap<> ();

        this.aspects = new AspectMapModel ();
    }

    public ChannelModel ( final ChannelModel other )
    {
        this.description = other.description;

        this.locked = other.locked;

        this.metaData = new HashMap<> ( other.metaData );

        // copy by ctor

        this.artifacts = other.artifacts.entrySet ().stream ().collect ( toMap ( Entry::getKey, entry -> new ArtifactModel ( entry.getValue () ) ) );
        this.cacheEntries = other.cacheEntries.entrySet ().stream ().collect ( toMap ( Entry::getKey, entry -> new CacheEntryModel ( entry.getValue () ) ) );

        this.aspects = new AspectMapModel ( other.aspects );
    }

    public void setDescription ( final String description )
    {
        this.description = description;
    }

    public String getDescription ()
    {
        return this.description;
    }

    public void setMetaData ( final Map<MetaKey, String> metaData )
    {
        this.metaData = metaData;
    }

    public Map<MetaKey, String> getMetaData ()
    {
        return this.metaData;
    }

    public void setLocked ( final boolean locked )
    {
        this.locked = locked;
    }

    public boolean isLocked ()
    {
        return this.locked;
    }

    public void addArtifact ( final ArtifactInformation ai )
    {
        this.artifacts.put ( ai.getId (), ArtifactModel.fromInformation ( ai ) );
    }

    public void removeArtifact ( final String artifactId )
    {
        this.artifacts.remove ( artifactId );
    }

    public Map<String, ArtifactModel> getArtifacts ()
    {
        return Collections.unmodifiableMap ( this.artifacts );
    }

    public AspectMapModel getAspects ()
    {
        return this.aspects;
    }

    public Map<MetaKey, CacheEntryModel> getCacheEntries ()
    {
        return this.cacheEntries;
    }
}
