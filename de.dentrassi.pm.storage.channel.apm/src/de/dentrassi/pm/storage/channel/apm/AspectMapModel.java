package de.dentrassi.pm.storage.channel.apm;

import java.util.Collection;
import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

public class AspectMapModel
{
    private final SortedMap<String, String> map;

    public AspectMapModel ()
    {
        this.map = new TreeMap<> ();
    }

    public AspectMapModel ( final AspectMapModel other )
    {
        this.map = new TreeMap<> ( other.map );
    }

    public void put ( final String aspectFactoryId, final String processedVersion )
    {
        this.map.put ( aspectFactoryId, processedVersion );
    }

    public void remove ( final String aspectFactoryId )
    {
        this.map.remove ( aspectFactoryId );
    }

    public Collection<String> getAspectIds ()
    {
        return Collections.unmodifiableCollection ( this.map.keySet () );
    }

    public SortedMap<String, String> getAspects ()
    {
        return Collections.unmodifiableSortedMap ( this.map );
    }
}
