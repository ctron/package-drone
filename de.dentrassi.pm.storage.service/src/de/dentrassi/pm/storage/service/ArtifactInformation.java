package de.dentrassi.pm.storage.service;

public class ArtifactInformation
{
    private final long length;

    private final String name;

    private final String channelId;

    public ArtifactInformation ( final long size, final String name, final String channelId )
    {
        this.length = size;
        this.name = name;
        this.channelId = channelId;
    }

    public long getLength ()
    {
        return this.length;
    }

    public String getName ()
    {
        return this.name;
    }

    public String getChannelId ()
    {
        return this.channelId;
    }
}
