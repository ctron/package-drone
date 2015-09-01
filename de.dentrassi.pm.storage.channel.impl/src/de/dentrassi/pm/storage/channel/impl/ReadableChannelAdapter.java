package de.dentrassi.pm.storage.channel.impl;

import de.dentrassi.pm.storage.channel.ChannelId;
import de.dentrassi.pm.storage.channel.ReadableChannel;
import de.dentrassi.pm.storage.channel.provider.AccessContext;

public class ReadableChannelAdapter implements ReadableChannel
{
    private final AccessContext context;

    private final ChannelId descriptor;

    public ReadableChannelAdapter ( final ChannelId descriptor, final AccessContext context )
    {
        this.descriptor = descriptor;
        this.context = context;
    }

    @Override
    public ChannelId getId ()
    {
        return this.descriptor;
    }

    @Override
    public AccessContext getContext ()
    {
        return this.context;
    }
}
