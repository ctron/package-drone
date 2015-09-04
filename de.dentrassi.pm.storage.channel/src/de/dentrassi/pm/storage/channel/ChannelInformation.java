package de.dentrassi.pm.storage.channel;

import java.util.Collections;
import java.util.SortedMap;

import de.dentrassi.pm.common.MetaKey;

public class ChannelInformation extends ChannelId
{
    private final ChannelState state;

    private final SortedMap<MetaKey, String> metaData;

    private final SortedMap<String, String> aspectStates;

    public ChannelInformation ( final ChannelId id, final ChannelState state, final SortedMap<MetaKey, String> metaData, final SortedMap<String, String> aspectStates )
    {
        this ( id.getId (), id.getName (), state, metaData, aspectStates );
    }

    private ChannelInformation ( final String id, final String name, final ChannelState state, final SortedMap<MetaKey, String> metaData, final SortedMap<String, String> aspectStates )
    {
        super ( id, name );

        this.state = state;
        this.metaData = Collections.unmodifiableSortedMap ( metaData );
        this.aspectStates = Collections.unmodifiableSortedMap ( aspectStates );
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

    public SortedMap<String, String> getAspectStates ()
    {
        return this.aspectStates;
    }

    public boolean hasAspect ( final String aspectId )
    {
        return this.aspectStates.containsKey ( aspectId );
    }
}
