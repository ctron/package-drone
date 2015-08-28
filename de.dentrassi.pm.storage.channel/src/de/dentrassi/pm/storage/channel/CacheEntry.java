package de.dentrassi.pm.storage.channel;

import java.io.InputStream;

public class CacheEntry extends CacheEntryInformation
{
    private final InputStream stream;

    public CacheEntry ( final CacheEntryInformation information, final InputStream stream )
    {
        super ( information );
        this.stream = stream;
    }

    public InputStream getStream ()
    {
        return this.stream;
    }

}
