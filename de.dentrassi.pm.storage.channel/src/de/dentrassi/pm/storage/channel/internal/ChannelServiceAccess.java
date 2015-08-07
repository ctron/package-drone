package de.dentrassi.pm.storage.channel.internal;

import java.util.Map;

public interface ChannelServiceAccess
{
    public String mapToId ( String name );

    public String mapToName ( String id );

    public Map<String, String> getMap ();
}
