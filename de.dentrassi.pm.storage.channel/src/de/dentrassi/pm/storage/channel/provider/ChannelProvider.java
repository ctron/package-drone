package de.dentrassi.pm.storage.channel.provider;

import java.util.Collection;

import de.dentrassi.pm.storage.channel.ChannelDetails;

public interface ChannelProvider
{
    public interface Listener
    {
        public void update ( Collection<Channel> added, Collection<Channel> removed );
    }

    public void addListener ( Listener listener );

    public void removeListener ( Listener listener );

    public Channel create ( ChannelDetails details );

    public ProviderInformation getInformation ();

    public default String getId ()
    {
        return getInformation ().getId ();
    }
}
