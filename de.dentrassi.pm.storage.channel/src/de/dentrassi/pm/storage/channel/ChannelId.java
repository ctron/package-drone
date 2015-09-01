package de.dentrassi.pm.storage.channel;

import java.util.Comparator;

public class ChannelId
{
    private final String id;

    private final String name;

    public ChannelId ( final String id, final String name )
    {
        this.id = id;
        this.name = name;
    }

    public String getId ()
    {
        return this.id;
    }

    public String getName ()
    {
        return this.name;
    }

    public static Comparator<? super ChannelId> NAME_COMPARATOR = Comparator.nullsLast ( Comparator.comparing ( ChannelId::getName ) );
}
