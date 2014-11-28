package de.dentrassi.pm.storage.web;

import de.dentrassi.pm.storage.service.Channel;

public class Functions
{
    public static String channel ( final Channel channel )
    {
        if ( channel.getName () == null )
        {
            return channel.getId ();
        }
        else
        {
            return String.format ( "%s (%s)", channel.getName (), channel.getId () );
        }
    }
}
