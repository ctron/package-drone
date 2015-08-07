package de.dentrassi.pm.storage.channel.apm;

import static java.util.stream.Collectors.toMap;

import java.util.Collections;
import java.util.Date;
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

    public ChannelModel ()
    {
        this.metaData = new HashMap<> ();
        this.artifacts = new HashMap<> ();
    }

    public ChannelModel ( final ChannelModel other )
    {
        this.description = other.description;

        this.locked = other.locked;

        this.metaData = new HashMap<> ( other.metaData );
        this.artifacts = other.artifacts.entrySet ().stream ().collect ( toMap ( Entry::getKey, entry -> new ArtifactModel ( entry.getValue () ) ) );
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
        final ArtifactModel art = new ArtifactModel ();
        art.setName ( ai.getName () );
        art.setSize ( ai.getSize () );
        art.setDate ( new Date ( ai.getCreationInstant ().toEpochMilli () ) );
        this.artifacts.put ( ai.getId (), art );
    }

    public Map<String, ArtifactModel> getArtifacts ()
    {
        return Collections.unmodifiableMap ( this.artifacts );
    }

}
