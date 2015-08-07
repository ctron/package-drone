package de.dentrassi.pm.storage.channel;

public class ChannelNotFoundException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    private final String channelId;

    public ChannelNotFoundException ( final String channelId )
    {
        super ( String.format ( "Channel '%s' could not be found", channelId ) );
        this.channelId = channelId;
    }

    public String getChannelId ()
    {
        return this.channelId;
    }
}
