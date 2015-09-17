package de.dentrassi.pm.storage.channel.provider;

import java.util.Collection;

import de.dentrassi.pm.storage.channel.ChannelDetails;
import de.dentrassi.pm.storage.channel.IdTransformer;

public interface ChannelProvider
{
    public interface Listener
    {
        public void update ( Collection<? extends Channel> added, Collection<? extends Channel> removed );
    }

    public void addListener ( Listener listener );

    public void removeListener ( Listener listener );

    public Channel create ( ChannelDetails details, IdTransformer idTransformer );

    public ProviderInformation getInformation ();

    public default String getId ()
    {
        return getInformation ().getId ();
    }

    /**
     * Delete all channels which are currently provided by this provider
     */
    public void wipe ();
}
