package de.dentrassi.pm.storage.channel.internal;

import java.util.Collections;
import java.util.Map;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class ChannelServiceModify implements ChannelServiceAccess
{
    /**
     * Map from id to name
     */
    private final BiMap<String, String> map;

    private final Map<String, String> unmodMap;

    public ChannelServiceModify ( final BiMap<String, String> map )
    {
        this.map = map != null ? map : HashBiMap.create ();
        this.unmodMap = Collections.unmodifiableMap ( this.map );
    }

    public ChannelServiceModify ( final ChannelServiceModify other )
    {
        this.map = other != null ? HashBiMap.create ( other.map ) : HashBiMap.create ();
        this.unmodMap = Collections.unmodifiableMap ( this.map );
    }

    @Override
    public Map<String, String> getMap ()
    {
        return this.unmodMap;
    }

    @Override
    public String mapToId ( final String name )
    {
        return this.map.inverse ().get ( name );
    }

    @Override
    public String mapToName ( final String id )
    {
        return this.map.get ( id );
    }

    public void putMapping ( final String id, final String name )
    {
        if ( name == null || name.isEmpty () )
        {
            this.map.remove ( id );
            return;
        }

        final String oldId = this.map.inverse ().get ( name );
        if ( oldId != null )
        {
            if ( oldId.equals ( id ) )
            {
                // no change
                return;
            }
            throw new IllegalStateException ( String.format ( "There already is a channel with the name '%s'", name ) );
        }

        // put mapping

        this.map.put ( id, name );
    }

    public String deleteMapping ( final String id, final String name )
    {
        return this.map.remove ( id, name ) ? id : null;
    }

}
