package de.dentrassi.pm.storage.channel.impl;

import de.dentrassi.pm.storage.channel.ChannelId;
import de.dentrassi.pm.storage.channel.ModifiableChannel;
import de.dentrassi.pm.storage.channel.provider.ModifyContext;

public class ModifiableChannelAdapter extends ReadableChannelAdapter implements ModifiableChannel
{
    private final ModifyContext context;

    public ModifiableChannelAdapter ( final ChannelId descriptor, final ModifyContext context )
    {
        super ( descriptor, context );
        this.context = context;
    }

    @Override
    public ModifyContext getContext ()
    {
        return this.context;
    }
}
