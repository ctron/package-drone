package de.dentrassi.pm.storage.channel;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import de.dentrassi.pm.storage.channel.deploy.DeployGroup;
import de.dentrassi.pm.storage.channel.deploy.DeployKey;
import de.dentrassi.pm.storage.channel.provider.ProviderInformation;
import de.dentrassi.pm.storage.channel.stats.ChannelStatistics;

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

    @FunctionalInterface
    public interface ArtifactReceiver
    {
        public void consume ( ArtifactInformation artifact, InputStream stream ) throws IOException;
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
            return new By ( Type.COMPOSITE, new By[] { id ( nameOrId ), name ( nameOrId ) } );
        }
    }

    public Collection<ProviderInformation> getProviders ();

    public Collection<ChannelInformation> list ();

    public Optional<ChannelInformation> getState ( By by );

    public ChannelId create ( String providerId, ChannelDetails details );

    public boolean delete ( By by );

    /**
     * @param by
     * @param clazz
     * @param operation
     * @return
     * @throws ChannelNotFoundException
     */
    public <R, T> R accessCall ( By by, Class<T> clazz, ChannelOperation<R, T> operation );

    /**
     * @param by
     * @param clazz
     * @param operation
     * @throws ChannelNotFoundException
     */
    public default <T> void accessRun ( final By by, final Class<T> clazz, final ChannelOperationVoid<T> operation )
    {
        accessCall ( by, clazz, channel -> {
            operation.process ( channel );
            return null;
        } );
    }

    public Map<String, String> getUnclaimedMappings ();

    public void deleteMapping ( String id, String name );

    public default Optional<Collection<DeployKey>> getChannelDeployKeys ( final String channelId )
    {
        return getChannelDeployGroups ( channelId ).map ( groups -> groups.stream ().flatMap ( group -> group.getKeys ().stream () ).collect ( toList () ) );
    }

    public default Optional<Set<String>> getChannelDeployKeyStrings ( final String channelId )
    {
        return getChannelDeployGroups ( channelId ).map ( groups -> groups.stream ().flatMap ( group -> group.getKeys ().stream () ).map ( DeployKey::getKey ).collect ( toSet () ) );
    }

    public Optional<Collection<DeployGroup>> getChannelDeployGroups ( String channelId );

    public default boolean streamArtifact ( final String channelId, final String artifactId, final ArtifactReceiver receiver )
    {
        try
        {
            return accessCall ( By.id ( channelId ), ReadableChannel.class, channel -> {

                final Optional<ChannelArtifactInformation> artifact = channel.getArtifact ( artifactId );
                if ( !artifact.isPresent () )
                {
                    return false;
                }

                return channel.getContext ().stream ( artifactId, stream -> {
                    receiver.consume ( artifact.get (), stream );
                } );
            } );
        }
        catch ( final ChannelNotFoundException e )
        {
            return false;
        }
    }

    /**
     * Delete all and everything
     */
    public void wipeClean ();

    public ChannelStatistics getStatistics ();
}
