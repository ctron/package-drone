package de.dentrassi.pm.storage.channel;

import java.time.Instant;
import java.util.Date;

import de.dentrassi.pm.common.MetaKey;

public class CacheEntryInformation
{
    private final MetaKey key;

    private final String name;

    private final long size;

    private final String mimeType;

    private final Instant timestamp;

    public CacheEntryInformation ( final MetaKey key, final String name, final long size, final String mimeType, final Instant timestamp )
    {
        this.key = key;
        this.name = name;
        this.size = size;
        this.mimeType = mimeType;
        this.timestamp = timestamp;
    }

    protected CacheEntryInformation ( final CacheEntryInformation other )
    {
        this.key = other.key;
        this.name = other.name;
        this.size = other.size;
        this.mimeType = other.mimeType;
        this.timestamp = other.timestamp;
    }

    public MetaKey getKey ()
    {
        return this.key;
    }

    public String getName ()
    {
        return this.name;
    }

    public long getSize ()
    {
        return this.size;
    }

    public String getMimeType ()
    {
        return this.mimeType;
    }

    public Instant getTimestamp ()
    {
        return this.timestamp;
    }

    public Date getTimestampAsDate ()
    {
        return new Date ( this.timestamp.toEpochMilli () );
    }
}
