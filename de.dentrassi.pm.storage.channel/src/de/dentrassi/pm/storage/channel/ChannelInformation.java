package de.dentrassi.pm.storage.channel;

import java.util.Collections;
import java.util.SortedMap;

import de.dentrassi.pm.common.MetaKey;

public class ChannelInformation extends ChannelId
{
    private final ChannelState state;

    private final SortedMap<MetaKey, String> metaData;

    public ChannelInformation ( final ChannelId id, final ChannelState state, final SortedMap<MetaKey, String> metaData )
    {
        this ( id.getId (), id.getName (), state, metaData );
    }

    private ChannelInformation ( final String id, final String name, final ChannelState state, final SortedMap<MetaKey, String> metaData )
    {
        super ( id, name );
        this.state = state;
        this.metaData = Collections.unmodifiableSortedMap ( metaData );
    }

    public ChannelState getState ()
    {
        return this.state;
    }

    public SortedMap<MetaKey, String> getMetaData ()
    {
        return this.metaData;
    }

    public String getMetaData ( final MetaKey key )
    {
        return this.metaData.get ( key );
    }

    public String getMetaData ( final String namespace, final String key )
    {
        return getMetaData ( new MetaKey ( namespace, key ) );
    }

}
