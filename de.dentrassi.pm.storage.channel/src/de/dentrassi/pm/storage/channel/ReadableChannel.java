package de.dentrassi.pm.storage.channel;

import de.dentrassi.pm.storage.channel.provider.AccessContext;

public interface ReadableChannel
{
    public ChannelId getDescriptor ();

    public AccessContext getContext ();

    public default ChannelInformation getInformation ()
    {
        final AccessContext ctx = getContext ();
        return new ChannelInformation ( getDescriptor (), ctx.getState (), ctx.getMetaData () );
    }
}
