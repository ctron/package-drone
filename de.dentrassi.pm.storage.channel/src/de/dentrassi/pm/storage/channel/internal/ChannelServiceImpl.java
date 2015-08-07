package de.dentrassi.pm.storage.channel.internal;

import static de.dentrassi.osgi.utils.Locks.lock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import org.eclipse.scada.utils.str.Tables;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import de.dentrassi.osgi.utils.Locks.Locked;
import de.dentrassi.pm.apm.StorageManager;
import de.dentrassi.pm.apm.StorageRegistration;
import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.storage.channel.ChannelDetails;
import de.dentrassi.pm.storage.channel.ChannelId;
import de.dentrassi.pm.storage.channel.ChannelInformation;
import de.dentrassi.pm.storage.channel.ChannelNotFoundException;
import de.dentrassi.pm.storage.channel.ChannelService;
import de.dentrassi.pm.storage.channel.DescriptorAdapter;
import de.dentrassi.pm.storage.channel.ModifiableChannel;
import de.dentrassi.pm.storage.channel.ReadableChannel;
import de.dentrassi.pm.storage.channel.provider.AccessContext;
import de.dentrassi.pm.storage.channel.provider.Channel;
import de.dentrassi.pm.storage.channel.provider.ChannelProvider;
import de.dentrassi.pm.storage.channel.provider.ChannelProvider.Listener;
import de.dentrassi.pm.storage.channel.provider.ModifyContext;
import de.dentrassi.pm.storage.channel.provider.ProviderInformation;

public class ChannelServiceImpl implements ChannelService
{

    public class DescriptorAdapterImpl implements DescriptorAdapter
    {
        @SuppressWarnings ( "unused" )
        private final ChannelEntry channel;

        private ChannelId descriptor;

        public DescriptorAdapterImpl ( final ChannelEntry channel )
        {
            this.channel = channel;
            this.descriptor = channel.getId ();
        }

        @Override
        public void setName ( final String name )
        {
            this.descriptor = new ChannelId ( this.descriptor.getId (), name );
        }

        @Override
        public ChannelId getDescriptor ()
        {
            return this.descriptor;
        }
    }

    private class Entry implements Listener
    {
        private final ChannelProvider service;

        private final Map<String, Channel> channels = new HashMap<> ();

        public Entry ( final ChannelProvider service )
        {
            this.service = service;

            this.service.addListener ( this );

            addProvider ( service );
        }

        public void dispose ()
        {
            removeProvider ( this.service );

            this.service.removeListener ( this );
            handleUpdate ( this.service, null, this.channels.values () );
            this.channels.clear ();
        }

        @Override
        public void update ( final Collection<Channel> added, final Collection<Channel> removed )
        {
            if ( added != null )
            {
                added.forEach ( channel -> this.channels.put ( channel.getId (), channel ) );
            }
            if ( removed != null )
            {
                removed.forEach ( channel -> this.channels.remove ( channel.getId () ) );
            }
            handleUpdate ( this.service, added, removed );
        }

    }

    private static class ChannelEntry
    {
        private ChannelId id;

        private final Channel channel;

        private final ChannelProvider provider;

        public ChannelEntry ( final ChannelId id, final Channel channel, final ChannelProvider provider )
        {
            this.id = id;
            this.channel = channel;
            this.provider = provider;
        }

        public ChannelId getId ()
        {
            return this.id;
        }

        public void setId ( final ChannelId descriptor )
        {
            this.id = descriptor;
        }

        public Channel getChannel ()
        {
            return this.channel;
        }

        public ChannelProvider getProvider ()
        {
            return this.provider;
        }
    }

    private static final MetaKey KEY_STORAGE = new MetaKey ( "channels", "service" );

    private final BundleContext context;

    private final ServiceTrackerCustomizer<ChannelProvider, Entry> customizer = new ServiceTrackerCustomizer<ChannelProvider, ChannelServiceImpl.Entry> () {

        @Override
        public Entry addingService ( final ServiceReference<ChannelProvider> reference )
        {
            return new Entry ( ChannelServiceImpl.this.context.getService ( reference ) );
        }

        @Override
        public void modifiedService ( final ServiceReference<ChannelProvider> reference, final Entry service )
        {
        }

        @Override
        public void removedService ( final ServiceReference<ChannelProvider> reference, final Entry service )
        {
            service.dispose ();
        }

    };

    private final Lock readLock;

    private final Lock writeLock;

    private final ServiceTracker<ChannelProvider, Entry> tracker;

    private final Map<String, ChannelEntry> channelMap = new HashMap<> ();

    private final Map<String, ChannelProvider> providerMap = new HashMap<> ();

    private final Set<ProviderInformation> providers = new CopyOnWriteArraySet<> ();

    private final Set<ProviderInformation> unmodProviders = Collections.unmodifiableSet ( this.providers );

    private StorageManager manager;

    private StorageRegistration handle;

    public ChannelServiceImpl ()
    {
        this.context = FrameworkUtil.getBundle ( ChannelServiceImpl.class ).getBundleContext ();

        final ReentrantReadWriteLock lock = new ReentrantReadWriteLock ();

        this.readLock = lock.readLock ();
        this.writeLock = lock.writeLock ();

        this.tracker = new ServiceTracker<ChannelProvider, Entry> ( this.context, ChannelProvider.class, this.customizer );
    }

    public void setStorageManager ( final StorageManager manager )
    {
        this.manager = manager;
    }

    public void handleUpdate ( final ChannelProvider provider, final Collection<Channel> added, final Collection<Channel> removed )
    {
        try ( final Locked l = lock ( this.writeLock ) )
        {
            // process additions

            if ( added != null )
            {
                added.forEach ( channel -> {
                    final String mappedId = makeMappedId ( provider, channel );
                    final String name = mapName ( mappedId );
                    this.channelMap.put ( mappedId, new ChannelEntry ( new ChannelId ( mappedId, name ), channel, provider ) );
                } );
            }

            // process removals

            if ( removed != null )
            {
                removed.forEach ( channel -> {
                    final String mappedId = makeMappedId ( provider, channel );
                    this.channelMap.remove ( mappedId );
                } );
            }
        }
    }

    private String mapName ( final String mappedId )
    {
        return this.manager.accessCall ( KEY_STORAGE, ChannelServiceAccess.class, model -> model.mapToName ( mappedId ) );
    }

    private String makeMappedId ( final ChannelProvider provider, final Channel channel )
    {
        return String.format ( "%s_%s", provider.getId (), channel.getId () );
    }

    public void start ()
    {
        this.handle = this.manager.registerModel ( 10_000, KEY_STORAGE, new ChannelServiceModelProvider () );

        try ( Locked l = lock ( this.writeLock ) )
        {
            this.tracker.open ();
        }
    }

    public void stop ()
    {
        try ( Locked l = lock ( this.writeLock ) )
        {
            this.tracker.close ();
        }

        if ( this.handle != null )
        {
            this.handle.unregister ();
            this.handle = null;
        }
    }

    private static ChannelInformation accessState ( final ChannelEntry channelEntry )
    {
        return accessRead ( channelEntry, channel -> channel.getInformation () );
    }

    @Override
    public Collection<ChannelInformation> list ()
    {
        try ( Locked l = lock ( this.readLock ) )
        {
            return this.channelMap.values ().stream ().map ( ChannelServiceImpl::accessState ).collect ( Collectors.toList () );
        }
    }

    /**
     * Find by the locator
     * <p>
     * This method does not acquire the read lock, this has to be done by the
     * caller
     * </p>
     *
     * @param by
     *            the locator
     * @return the result
     */
    protected Optional<ChannelEntry> find ( final By by )
    {
        switch ( by.getType () )
        {
            case ID:
                return Optional.ofNullable ( this.channelMap.get ( by.getQualifier () ) );
            case NAME:
                return findByName ( (String)by.getQualifier () );
            case COMPOSITE:
            {
                final By[] bys = (By[])by.getQualifier ();
                for ( final By oneBy : bys )
                {
                    final Optional<ChannelEntry> result = find ( oneBy );
                    if ( result.isPresent () )
                    {
                        return result;
                    }
                }
                return Optional.empty ();
            }
            default:
                throw new IllegalArgumentException ( String.format ( "Unknown locator type: %s", by.getType () ) );
        }
    }

    /**
     * Find a channel by name
     * <p>
     * FIXME: improve performance
     * </p>
     *
     * @param name
     *            the channel name to look for
     * @return the optional channel entry, never returns {@code null} but my
     *         return {@link Optional#empty()}.
     */
    private Optional<ChannelEntry> findByName ( final String name )
    {
        if ( name == null )
        {
            return Optional.empty ();
        }

        for ( final ChannelEntry entry : this.channelMap.values () )
        {
            if ( name.equals ( entry.getId ().getName () ) )
            {
                Optional.of ( entry );
            }
        }

        return Optional.empty ();
    }

    @Override
    public Optional<ChannelInformation> getState ( final By by )
    {
        try ( Locked l = lock ( this.readLock ) )
        {
            return find ( by ).map ( ChannelServiceImpl::accessState );
        }
    }

    public void addProvider ( final ChannelProvider provider )
    {
        try ( Locked l = lock ( this.writeLock ) )
        {
            final ProviderInformation info = provider.getInformation ();

            this.providerMap.put ( info.getId (), provider );
            this.providers.add ( info );
        }
    }

    public void removeProvider ( final ChannelProvider provider )
    {
        try ( Locked l = lock ( this.writeLock ) )
        {
            final ProviderInformation info = provider.getInformation ();

            this.providerMap.remove ( info.getId () );
            this.providers.remove ( info );
        }
    }

    @Override
    public Collection<ProviderInformation> getProviders ()
    {
        try ( Locked l = lock ( this.readLock ) )
        {
            return this.unmodProviders;
        }
    }

    @Override
    public ChannelId create ( final String providerId, final ChannelDetails description )
    {
        ChannelProvider provider;
        try ( Locked l = lock ( this.readLock ) )
        {
            if ( providerId != null )
            {
                provider = this.providerMap.get ( providerId );
            }
            else if ( this.providerMap.size () == 1 )
            {
                provider = this.providerMap.values ().iterator ().next ();
            }
            else
            {
                throw new IllegalArgumentException ( "No provider selected, but there is more than one provider available." );
            }
        }

        final Channel channel = provider.create ( description );

        final String id = makeMappedId ( provider, channel );
        return new ChannelId ( id, mapName ( id ) );
    }

    @Override
    public boolean delete ( final By by )
    {
        try ( Locked l = lock ( this.writeLock ) )
        {
            final Optional<ChannelEntry> channel = find ( by );
            if ( !channel.isPresent () )
            {
                return false;
            }

            final ChannelEntry entry = channel.get ();

            // explicitly delete the mapping
            deleteMapping ( entry.getId ().getId (), entry.getId ().getName () );
            handleUpdate ( entry.getProvider (), null, Collections.singleton ( entry.getChannel () ) );

            entry.getChannel ().delete ();

            return true;
        }
    }

    @SuppressWarnings ( "unchecked" )
    @Override
    public <R, T> R access ( final By by, final Class<T> clazz, final ChannelOperation<R, T> operation )
    {
        if ( ReadableChannel.class.equals ( clazz ) )
        {
            return accessRead ( findChannel ( by ), (ChannelOperation<R, ReadableChannel>)operation );
        }
        else if ( ModifiableChannel.class.equals ( clazz ) )
        {
            return accessModify ( findChannel ( by ), (ChannelOperation<R, ModifiableChannel>)operation );
        }
        else if ( DescriptorAdapter.class.equals ( clazz ) )
        {
            try ( Locked l = lock ( this.writeLock ) )
            {
                return handleDescribe ( findChannel ( by ), (ChannelOperation<R, DescriptorAdapter>)operation );
            }
        }
        else
        {
            throw new IllegalArgumentException ( String.format ( "Unknown channel adapter: %s", clazz.getName () ) );
        }
    }

    private static <R> R accessRead ( final ChannelEntry channelEntry, final ChannelOperation<R, ReadableChannel> operation )
    {
        return channelEntry.getChannel ().access ( ctx -> {

            try ( Disposing<AccessContext> wrappedCtx = Disposing.proxy ( AccessContext.class, ctx );
                  Disposing<ReadableChannel> channel = Disposing.proxy ( ReadableChannel.class, new ReadableChannelAdapter ( channelEntry.getId (), wrappedCtx.getTarget () ) ) )
            {
                return operation.process ( channel.getTarget () );
            }
        } );
    }

    private static <T, R> R accessModify ( final ChannelEntry channelEntry, final ChannelOperation<R, ModifiableChannel> operation )
    {
        return channelEntry.getChannel ().modify ( ctx -> {
            try ( Disposing<ModifyContext> wrappedCtx = Disposing.proxy ( ModifyContext.class, ctx );
                  Disposing<ModifiableChannel> channel = Disposing.proxy ( ModifiableChannel.class, new ModifiableChannelAdapter ( channelEntry.getId (), wrappedCtx.getTarget () ) ) )
            {
                return operation.process ( ModifiableChannel.class.cast ( new ModifiableChannelAdapter ( channelEntry.getId (), ctx ) ) );
            }
        } );
    }

    private <R> R handleDescribe ( final ChannelEntry channel, final ChannelOperation<R, DescriptorAdapter> operation )
    {
        return this.manager.modifyCall ( KEY_STORAGE, ChannelServiceModify.class, model -> {

            final DescriptorAdapter dai = new DescriptorAdapterImpl ( channel) {
                @Override
                public void setName ( final String name )
                {
                    model.putMapping ( getDescriptor ().getId (), name );

                    super.setName ( name );

                    final ChannelId desc = getDescriptor ();
                    StorageManager.executeAfterPersist ( () -> {
                        channel.setId ( desc );
                    } );
                }
            };

            try ( Disposing<DescriptorAdapter> adapter = Disposing.proxy ( DescriptorAdapter.class, dai ) )
            {
                return operation.process ( adapter.getTarget () );
            }
            catch ( final Exception e )
            {
                throw new RuntimeException ( e );
            }
        } );
    }

    private ChannelEntry findChannel ( final By by )
    {
        final Optional<ChannelEntry> channel;
        try ( Locked l = lock ( this.readLock ) )
        {
            channel = find ( by );
        }

        if ( !channel.isPresent () )
        {
            throw new ChannelNotFoundException ( "fixme" );
        }

        return channel.get ();
    }

    @Override
    public Map<String, String> getUnclaimedMappings ()
    {
        try ( Locked l = lock ( this.readLock ) )
        {
            return this.manager.modifyCall ( KEY_STORAGE, ChannelServiceModify.class, model -> {

                final Map<String, String> map = model.getMap ();

                for ( final ChannelEntry entry : this.channelMap.values () )
                {
                    map.remove ( entry.getId () );
                }

                return map;
            } );
        }
    }

    /**
     * This is a console command
     */
    public void listUnclaimedMappings ()
    {
        final Map<String, String> map = getUnclaimedMappings ();

        final List<List<String>> rows = new ArrayList<> ( map.size () );

        for ( final Map.Entry<String, String> entry : map.entrySet () )
        {
            final ArrayList<String> row = new ArrayList<> ( 2 );
            row.add ( entry.getKey () );
            row.add ( entry.getValue () );
            rows.add ( row );
        }

        Tables.showTable ( System.out, Arrays.asList ( "ID", "Name" ), rows, 2 );
    }

    @Override
    public void deleteMapping ( final String id, final String name )
    {
        try ( Locked l = lock ( this.writeLock ) )
        {
            this.manager.modifyRun ( KEY_STORAGE, ChannelServiceModify.class, model -> {

                final String affectedId = model.deleteMapping ( id, name );
                if ( affectedId != null )
                {
                    StorageManager.executeAfterPersist ( () -> {
                        // try to remove from mapped channels
                        final ChannelEntry channel = this.channelMap.get ( id );
                        if ( channel != null )
                        {
                            channel.id = new ChannelId ( id, null );
                        }
                    } );
                }
            } );
        }
    }

}
