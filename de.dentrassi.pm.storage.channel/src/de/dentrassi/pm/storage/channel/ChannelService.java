package de.dentrassi.pm.storage.channel;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import de.dentrassi.pm.storage.channel.provider.ProviderInformation;

public interface ChannelService
{
    @FunctionalInterface
    public interface ChannelOperation<R, T>
    {
        public R process ( T channel ) throws Exception;
    }

    @FunctionalInterface
    public interface ChannelOperationVoid<T>
    {
        public void process ( T channel ) throws Exception;
    }

    public static final class By
    {
        public static enum Type
        {
            ID,
            NAME,
            COMPOSITE;
        }

        private final Type type;

        private final Object qualifier;

        private By ( final Type type, final Object qualifier )
        {
            this.type = type;
            this.qualifier = qualifier;
        }

        public Type getType ()
        {
            return type;
        }

        public Object getQualifier ()
        {
            return qualifier;
        }

        public static By id ( final String channelId )
        {
            return new By ( Type.ID, channelId );
        }

        public static By name ( final String name )
        {
            return new By ( Type.NAME, name );
        }

        public static By nameOrId ( final String nameOrId )
        {
            return new By ( Type.COMPOSITE, new Object[] { id ( nameOrId ), name ( nameOrId ) } );
        }
    }

    public Collection<ProviderInformation> getProviders ();

    public Collection<ChannelInformation> list ();

    public Optional<ChannelInformation> getState ( By by );

    public ChannelId create ( String providerId, ChannelDetails details );

    public boolean delete ( By by );

    public <R, T> R access ( By by, Class<T> clazz, ChannelOperation<R, T> operation );

    public default <T> void access ( final By by, final Class<T> clazz, final ChannelOperationVoid<T> operation )
    {
        access ( by, clazz, channel -> {
            operation.process ( channel );
            return null;
        } );
    }

    public Map<String, String> getUnclaimedMappings ();

    public void deleteMapping ( String id, String name );
}
