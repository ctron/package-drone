package de.dentrassi.pm.storage.channel;

import java.util.Map;

import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.storage.channel.provider.ModifyContext;

public interface ModifiableChannel extends ReadableChannel
{
    @Override
    public ModifyContext getContext ();

    public default void setDescription ( final ChannelDetails description )
    {
        getContext ().setDetails ( description );
    }

    public default void applyMetaData ( final Map<MetaKey, String> changes )
    {
        getContext ().applyMetaData ( changes );
    }

    public default void lock ()
    {
        getContext ().lock ();
    }

    public default void unlock ()
    {
        getContext ().unlock ();
    }
}
