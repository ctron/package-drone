package de.dentrassi.pm.storage.channel;

import java.util.Comparator;

public class ChannelId
{
    private final String id;

    private final String name;

    public ChannelId ( final String id )
    {
        this ( id, null );
    }

    public ChannelId ( final String id, final String name )
    {
        this.id = id;
        this.name = name;

        if ( this.id == null )
        {
            throw new NullPointerException ( "'id' must not be null" );
        }
    }

    public String getId ()
    {
        return this.id;
    }

    public String getName ()
    {
        return this.name;
    }

    public String getNameOrId ()
    {
        if ( this.name != null && !this.name.isEmpty () )
        {
            return this.name;
        }
        return this.id;
    }

    public static Comparator<? super ChannelId> NAME_COMPARATOR = Comparator.comparing ( ChannelId::getName, Comparator.nullsLast ( Comparator.naturalOrder () ) ).thenComparing ( Comparator.comparing ( ChannelId::getId ) );
}
