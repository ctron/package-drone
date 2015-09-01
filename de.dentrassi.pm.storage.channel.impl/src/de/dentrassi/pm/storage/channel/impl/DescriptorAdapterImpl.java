package de.dentrassi.pm.storage.channel.impl;

import de.dentrassi.pm.storage.channel.ChannelId;
import de.dentrassi.pm.storage.channel.DescriptorAdapter;

class DescriptorAdapterImpl implements DescriptorAdapter
{
    private ChannelId descriptor;

    public DescriptorAdapterImpl ( final ChannelEntry channel )
    {
        this.descriptor = channel.getId ();
    }

    @Override
    public void setName ( final String name )
    {
        this.descriptor = new ChannelId ( this.descriptor.getId (), name );
    }

    @Override
    public ChannelId getDescriptor ()
    {
        return this.descriptor;
    }
}
