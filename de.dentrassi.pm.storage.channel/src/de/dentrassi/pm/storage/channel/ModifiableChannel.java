package de.dentrassi.pm.storage.channel;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.storage.channel.provider.ModifyContext;

public interface ModifiableChannel extends ReadableChannel, AspectableChannel
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

    @Override
    public default void addAspects ( final boolean withDependencies, final String... aspectIds )
    {
        // FIXME: with dependencies
        getContext ().addAspects ( new HashSet<> ( Arrays.asList ( aspectIds ) ) );
    }

    @Override
    public default void removeAspects ( final String... aspectIds )
    {
        getContext ().removeAspects ( new HashSet<> ( Arrays.asList ( aspectIds ) ) );
    }

    @Override
    default void refreshAspects ( final String... aspectIds )
    {
        getContext ().refreshAspects ( new HashSet<> ( Arrays.asList ( aspectIds ) ) );
    }
}
