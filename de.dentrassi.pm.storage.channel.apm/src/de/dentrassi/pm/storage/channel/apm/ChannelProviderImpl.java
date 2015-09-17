package de.dentrassi.pm.storage.channel.apm;

import static de.dentrassi.pm.storage.channel.apm.ChannelModelProvider.makeBasePath;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.scada.utils.io.RecursiveDeleteVisitor;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dentrassi.pm.apm.StorageManager;
import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.storage.channel.ChannelDetails;
import de.dentrassi.pm.storage.channel.IdTransformer;
import de.dentrassi.pm.storage.channel.provider.Channel;
import de.dentrassi.pm.storage.channel.provider.ChannelProvider;
import de.dentrassi.pm.storage.channel.provider.ProviderInformation;

/**
 * An APM based channel provider
 * <p>
 * <em>Note: </em> the channel provider implementation is not thread safe. It
 * must be guaranteed by the caller (normally the ChannelService) that calls to
 * the channel provider implementation are mutually exclusive.
 * </p>
 */
public class ChannelProviderImpl implements ChannelProvider
{
    private final static Logger logger = LoggerFactory.getLogger ( ChannelProviderImpl.class );

    private static final ProviderInformation INFO = new ProviderInformation ( "apm", "APM storage", "APM based channel storage" );

    private StorageManager manager;

    private EventAdmin eventAdmin;

    private final CopyOnWriteArraySet<Listener> listeners = new CopyOnWriteArraySet<> ();

    private final CopyOnWriteArraySet<ChannelImpl> channels = new CopyOnWriteArraySet<> ();

    public ChannelProviderImpl ()
    {
    }

    public void setManager ( final StorageManager manager )
    {
        this.manager = manager;
    }

    public void setEventAdmin ( final EventAdmin eventAdmin )
    {
        this.eventAdmin = eventAdmin;
    }

    public void start ()
    {
        final Path base = this.manager.getContext ().getBasePath ().resolve ( "channels" );

        try
        {
            Files.list ( base ).forEach ( child -> {
                if ( !Files.isDirectory ( child ) )
                {
                    return;
                }

                try
                {
                    final UUID id = UUID.fromString ( child.getName ( child.getNameCount () - 1 ).toString () );
                    discoveredChannel ( id.toString () );
                }
                catch ( final IllegalArgumentException e )
                {
                    // invalid format
                    logger.info ( String.format ( "Failed to use '%s' as a channel directory", child ), e );
                }
            } );
        }
        catch ( final NoSuchFileException e )
        {
            // ignore the non-existence of the directory
        }
        catch ( final IOException e )
        {
            logger.warn ( "Failed to scan for channels", e );
        }
    }

    public void stop ()
    {
        fireChange ( null, new CopyOnWriteArraySet<> ( this.channels ) );

        for ( final ChannelImpl channel : this.channels )
        {
            channel.dispose ();
        }
        this.channels.clear ();
    }

    @Override
    public void addListener ( final Listener listener )
    {
        if ( this.listeners.add ( listener ) )
        {
            // send known
            fireChange ( new CopyOnWriteArraySet<> ( this.channels ), null );
        }
    }

    @Override
    public void removeListener ( final Listener listener )
    {
        this.listeners.remove ( listener );
    }

    protected void fireChange ( final Collection<? extends Channel> added, final Collection<? extends Channel> removed )
    {
        this.listeners.forEach ( listener -> listener.update ( added, removed ) );
    }

    @Override
    public Channel create ( final ChannelDetails details, final IdTransformer idTransformer )
    {
        final ChannelImpl channel = createNewChannel ( details, idTransformer );
        registerChannel ( channel );
        return channel;
    }

    private void discoveredChannel ( final String channelId )
    {
        final MetaKey key = new MetaKey ( "channel", channelId );
        final ChannelImpl channel = new ChannelImpl ( channelId, this.eventAdmin, key, this.manager, this );
        registerChannel ( channel );
    }

    protected ChannelImpl createNewChannel ( final ChannelDetails details, final IdTransformer idTransformer )
    {
        final String id = UUID.randomUUID ().toString ();
        final MetaKey key = new MetaKey ( "channel", id );

        final ChannelImpl channel = new ChannelImpl ( id, this.eventAdmin, key, this.manager, this );

        // we always call modify, in order to persist the channel at least once
        channel.modify ( model -> {
            if ( details != null )
            {
                model.setDetails ( details );
            }
        } , idTransformer );
        return channel;
    }

    private void registerChannel ( final ChannelImpl channel )
    {
        this.channels.add ( channel );
        fireChange ( Collections.singleton ( channel ), null );
    }

    public void deleteChannel ( final ChannelImpl channel )
    {
        if ( this.channels.remove ( channel ) )
        {
            final String id = channel.getId ();
            fireChange ( null, Collections.singleton ( channel ) );
            channel.dispose ();
            deleteChannelContent ( id );
        }
    }

    private void deleteChannelContent ( final String id )
    {
        Path path = makeBasePath ( this.manager.getContext (), id );

        final Path dir = path.getParent ();
        final Path fn = path.getFileSystem ().getPath ( "x-" + id );
        final Path target = dir == null ? fn : dir.resolve ( fn );
        try
        {
            Files.move ( path, target, StandardCopyOption.ATOMIC_MOVE );
            path = target;
        }
        catch ( final IOException e )
        {
            logger.warn ( "Failed to rename the channel directoy first", e );
        }

        try
        {
            Files.walkFileTree ( path, new RecursiveDeleteVisitor () );
        }
        catch ( final NoSuchFileException e )
        {
            // ignore
        }
        catch ( final IOException e )
        {
            throw new RuntimeException ( "Failed to delete channel content", e );
        }
    }

    @Override
    public void wipe ()
    {
        final CopyOnWriteArrayList<ChannelImpl> deletedChannels = new CopyOnWriteArrayList<> ( this.channels );
        this.channels.clear ();

        for ( final Channel channel : deletedChannels )
        {
            try
            {
                ( (ChannelImpl)channel ).dispose ();
                deleteChannelContent ( channel.getId () );
            }
            catch ( final Exception e )
            {
                logger.warn ( "Failed to wipe/delete channel: " + channel.getId (), e );
            }
        }

        fireChange ( null, deletedChannels );
    }

    @Override
    public ProviderInformation getInformation ()
    {
        return INFO;
    }
}
