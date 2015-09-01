package de.dentrassi.pm.storage.channel;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.common.utils.IOConsumer;
import de.dentrassi.pm.storage.channel.provider.AccessContext;

public interface ReadableChannel
{
    public ChannelId getId ();

    public AccessContext getContext ();

    public default ChannelInformation getInformation ()
    {
        final AccessContext ctx = getContext ();
        return new ChannelInformation ( getId (), ctx.getState (), ctx.getMetaData (), ctx.getAspectStates () );
    }

    public default ChannelArtifactInformation withChannel ( final ArtifactInformation artifact )
    {
        if ( artifact == null )
        {
            return null;
        }
        return new ChannelArtifactInformation ( getId (), artifact );
    }

    public default Optional<ChannelArtifactInformation> getArtifact ( final String id )
    {
        return Optional.ofNullable ( withChannel ( getContext ().getArtifacts ().get ( id ) ) );
    }

    public default boolean hasAspect ( final String aspectId )
    {
        return getInformation ().getAspectStates ().containsKey ( aspectId );
    }

    public default Map<MetaKey, String> getMetaData ()
    {
        return getContext ().getMetaData ();
    }

    public default Map<MetaKey, CacheEntryInformation> getCacheEntries ()
    {
        return getContext ().getCacheEntries ();
    }

    public default boolean streamCacheEntry ( final MetaKey key, final IOConsumer<CacheEntry> consumer ) throws IOException
    {
        return getContext ().streamCacheEntry ( key, consumer );
    }
}
