package de.dentrassi.pm.storage.channel.internal;

import de.dentrassi.pm.storage.channel.ChannelId;
import de.dentrassi.pm.storage.channel.provider.Channel;
import de.dentrassi.pm.storage.channel.provider.ChannelProvider;

class ChannelEntry
{
    private ChannelId id;

    private final Channel channel;

    private final ChannelProvider provider;

    public ChannelEntry ( final ChannelId id, final Channel channel, final ChannelProvider provider )
    {
        this.id = id;
        this.channel = channel;
        this.provider = provider;
    }

    public ChannelId getId ()
    {
        return this.id;
    }

    public void setId ( final ChannelId id )
    {
        this.id = id;
    }

    public Channel getChannel ()
    {
        return this.channel;
    }

    public ChannelProvider getProvider ()
    {
        return this.provider;
    }
}
