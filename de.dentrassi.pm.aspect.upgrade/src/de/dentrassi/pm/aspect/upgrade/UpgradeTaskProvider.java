/*******************************************************************************
 * Copyright (c) 2015 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package de.dentrassi.pm.aspect.upgrade;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import de.dentrassi.osgi.web.LinkTarget;
import de.dentrassi.osgi.web.RequestMethod;
import de.dentrassi.pm.aspect.ChannelAspectFactory;
import de.dentrassi.pm.aspect.ChannelAspectProcessor;
import de.dentrassi.pm.common.ChannelAspectInformation;
import de.dentrassi.pm.common.Version;
import de.dentrassi.pm.common.web.Button;
import de.dentrassi.pm.common.web.Modifier;
import de.dentrassi.pm.storage.Channel;
import de.dentrassi.pm.storage.service.StorageService;
import de.dentrassi.pm.todo.BasicTask;
import de.dentrassi.pm.todo.DefaultTaskProvider;
import de.dentrassi.pm.todo.Task;

public class UpgradeTaskProvider extends DefaultTaskProvider implements EventHandler
{
    private final static Logger logger = LoggerFactory.getLogger ( UpgradeTaskProvider.class );

    private static final Button PERFORM_BUTTON = new Button ( "Refresh aspect", "refresh", Modifier.DEFAULT );

    private static final Button PERFORM_ALL_BUTTON = new Button ( "Refresh channel", "refresh", Modifier.DEFAULT );

    private final ServiceListener listener = new ServiceListener () {

        @Override
        public void serviceChanged ( final ServiceEvent event )
        {
            handleServiceChange ( event );
        }
    };

    private StorageService service;

    private BundleContext context;

    private ChannelAspectProcessor channelProcessor;

    public UpgradeTaskProvider ()
    {
    }

    public void setService ( final StorageService service )
    {
        this.service = service;
    }

    public void start () throws Exception
    {
        logger.info ( "Starting channel aspect upgrade watcher" );

        this.context = FrameworkUtil.getBundle ( UpgradeTaskProvider.class ).getBundleContext ();
        this.context.addServiceListener ( this.listener, String.format ( "(%s=%s)", Constants.OBJECTCLASS, ChannelAspectFactory.class.getName () ) );

        refresh ();
    }

    public void stop ()
    {
        this.context.removeServiceListener ( this.listener );
        this.channelProcessor.close ();
    }

    protected void handleServiceChange ( final ServiceEvent event )
    {
        logger.debug ( "service change - {} - {}", event.getType (), event.getServiceReference () );

        switch ( event.getType () )
        {
            case ServiceEvent.UNREGISTERING:
            case ServiceEvent.REGISTERED:
                refresh ();
                break;
        }
    }

    public void refresh ()
    {
        logger.info ( "Refreshing" );
        setTasks ( updateState () );
    }

    private List<Task> updateState ()
    {
        final Map<String, ChannelAspectInformation> infos = ChannelAspectProcessor.scanAspectInformations ( this.context );

        final List<Task> result = new LinkedList<> ();

        final Multimap<String, Channel> missing = HashMultimap.create ();

        final Multimap<Channel, String> channels = HashMultimap.create ();

        for ( final Channel channel : this.service.listChannels () )
        {
            logger.debug ( "Checking channel: {}", channel.getId () );

            final Map<String, String> states = channel.getAspectStates ();
            for ( final Map.Entry<String, String> entry : states.entrySet () )
            {
                logger.debug ( "\t{}", entry.getKey () );

                final ChannelAspectInformation info = infos.get ( entry.getKey () );
                if ( info == null )
                {
                    missing.put ( entry.getKey (), channel );
                }

                logger.debug ( "\t{} - {} -> {}", info.getFactoryId (), entry.getValue (), info.getVersion () );

                if ( !info.getVersion ().equals ( Version.valueOf ( entry.getValue () ) ) )
                {
                    result.add ( makeUpgradeTask ( channel, info, entry.getValue () ) );
                    channels.put ( channel, entry.getKey () );
                }
            }
        }

        for ( final Map.Entry<Channel, Collection<String>> entry : channels.asMap ().entrySet () )
        {
            final Channel channel = entry.getKey ();
            final LinkTarget target = new LinkTarget ( String.format ( "/channel/%s/refreshAllAspects", channel.getId () ) );
            final String description = "Channel aspects active in this channe have been updated. You can refresh the whole channel.";
            result.add ( new BasicTask ( "Refresh channel: " + makeChannelTitle ( channel ), 100, description, target, RequestMethod.GET, PERFORM_ALL_BUTTON ) );
        }

        for ( final Map.Entry<String, Collection<Channel>> entry : missing.asMap ().entrySet () )
        {
            final String missingChannels = entry.getValue ().stream ().map ( Channel::getId ).collect ( Collectors.joining ( ", " ) );
            result.add ( new BasicTask ( String.format ( "Fix missing channel aspect: %s", entry.getKey () ), 1, String.format ( "The channel aspect '%s' is being used but not installed in the system. Channels: %s", entry.getKey (), missingChannels ), null ) );
        }

        return result;
    }

    private String makeChannelTitle ( final Channel channel )
    {
        if ( channel.getName () != null )
        {
            return String.format ( "%s (%s)", channel.getName (), channel.getId () );
        }
        else
        {
            return channel.getId ();
        }
    }

    private Task makeUpgradeTask ( final Channel channel, final ChannelAspectInformation info, final String fromVersion )
    {
        final String channelName = makeChannelTitle ( channel );

        String factoryId;
        try
        {
            factoryId = URLEncoder.encode ( info.getFactoryId (), "UTF-8" );
        }
        catch ( final UnsupportedEncodingException e )
        {
            factoryId = info.getFactoryId ();
        }

        final LinkTarget target = new LinkTarget ( String.format ( "/channel/%s/refreshAspect?aspect=%s", channel.getId (), factoryId ) );

        final String description = String.format ( "The aspect %s (%s) in channel %s was upgraded from version %s to %s. The channel aspect has to be re-processed.", info.getLabel (), info.getFactoryId (), channelName, fromVersion, info.getVersion () );
        return new BasicTask ( "Upgrade aspect data", 1_000, description, target, RequestMethod.POST, PERFORM_BUTTON );
    }

    @Override
    public void handleEvent ( final Event event )
    {
        logger.debug ( "Received event - {}", event.getTopic () );

        final String topic = event.getTopic ();
        if ( topic.endsWith ( "/refresh" ) || topic.endsWith ( "/remove" ) )
        {
            refresh ();
        }
    }
}
