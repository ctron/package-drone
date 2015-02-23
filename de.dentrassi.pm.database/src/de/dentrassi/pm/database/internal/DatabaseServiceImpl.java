/*******************************************************************************
 * Copyright (c) 2015 IBH SYSTEMS GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package de.dentrassi.pm.database.internal;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dentrassi.osgi.utils.Dictionaries;
import de.dentrassi.pm.todo.TaskProvider;

public class DatabaseServiceImpl implements ManagedService
{
    private static final String GEMINI_FACTORY_PID = "gemini.jpa.punit";

    private static final String PROP_UNIT_NAME = "gemini.jpa.punit.name";

    private final static Logger logger = LoggerFactory.getLogger ( DatabaseServiceImpl.class );

    private Hashtable<String, Object> properties;

    public static final class DatabaseBundle
    {
        private final ConfigurationAdmin admin;

        private final String name;

        public DatabaseBundle ( final ConfigurationAdmin admin, final Bundle bundle )
        {
            this.admin = admin;
            this.name = bundle.getSymbolicName ();
        }

        public void create ( final Hashtable<String, Object> properties ) throws IOException
        {
            logger.info ( "Creating configuration for {}: {}", this.name, properties );

            final Hashtable<String, Object> props = new Hashtable<> ( properties );

            final Configuration cfg = this.admin.createFactoryConfiguration ( GEMINI_FACTORY_PID, null );
            props.put ( PROP_UNIT_NAME, this.name );
            cfg.update ( props );
        }

        public void delete () throws IOException, InvalidSyntaxException
        {
            logger.info ( "Deleting configuration for: {}", this.name );

            final Configuration[] result = this.admin.listConfigurations ( String.format ( "(&(%s=%s)(%s=%s))", "service.factoryPid", GEMINI_FACTORY_PID, PROP_UNIT_NAME, this.name ) );
            if ( result != null )
            {
                for ( final Configuration cfg : result )
                {
                    cfg.delete ();
                }
            }
        }
    }

    private final BundleTrackerCustomizer<DatabaseBundle> customizer = new BundleTrackerCustomizer<DatabaseServiceImpl.DatabaseBundle> () {

        @Override
        public void removedBundle ( final Bundle bundle, final BundleEvent event, final DatabaseBundle object )
        {
            logger.info ( "Remove bundle: {}", bundle.getSymbolicName () );
        }

        @Override
        public void modifiedBundle ( final Bundle bundle, final BundleEvent event, final DatabaseBundle object )
        {
            logger.info ( "Modified bundle: {}", bundle.getSymbolicName () );
        }

        @Override
        public DatabaseBundle addingBundle ( final Bundle bundle, final BundleEvent event )
        {
            if ( !isPersistenceUnit ( bundle ) )
            {
                return null;
            }

            logger.info ( "Adding bundle: {}", bundle.getSymbolicName () );
            final DatabaseBundle db = new DatabaseBundle ( DatabaseServiceImpl.this.admin, bundle );

            try
            {
                db.delete ();
                if ( DatabaseServiceImpl.this.properties != null )
                {
                    db.create ( DatabaseServiceImpl.this.properties );
                }
            }
            catch ( final Exception e )
            {
                logger.warn ( "Initial update failed", e );
            }

            return db;
        }
    };

    private ConfigurationAdmin admin;

    private final DatabaseTaskProvider taskProvider = new DatabaseTaskProvider ();

    public void setAdmin ( final ConfigurationAdmin admin )
    {
        this.admin = admin;
    }

    private final BundleContext context;

    private final BundleTracker<DatabaseBundle> tracker;

    private ServiceRegistration<?> handle;

    private ScheduledExecutorService executor;

    public DatabaseServiceImpl ()
    {
        this.context = FrameworkUtil.getBundle ( DatabaseServiceImpl.class ).getBundleContext ();
        this.tracker = new BundleTracker<> ( this.context, Bundle.INSTALLED | Bundle.ACTIVE | Bundle.RESOLVED | Bundle.STARTING | Bundle.STOPPING, this.customizer );
    }

    public void start ()
    {
        this.executor = Executors.newSingleThreadScheduledExecutor ();

        this.tracker.open ();
        {
            final Dictionary<String, Object> properties = new Hashtable<> ();
            properties.put ( Constants.SERVICE_DESCRIPTION, "Task provider for database configuration" );
            properties.put ( EventConstants.EVENT_TOPIC, new String[] { "packagedrone/database/schema" } );
            this.handle = this.context.registerService ( new String[] { TaskProvider.class.getName (), EventHandler.class.getName () }, this.taskProvider, properties );
        }
    }

    public void stop ()
    {
        if ( this.handle != null )
        {
            this.handle.unregister ();
            this.handle = null;
        }
        this.tracker.close ();

        this.executor.shutdown ();
    }

    protected boolean isPersistenceUnit ( final Bundle bundle )
    {
        if ( bundle.getHeaders ().get ( "Meta-Persistence" ) != null )
        {
            return true;
        }

        if ( bundle.getEntry ( "/META-INF/persistence.xml" ) != null )
        {
            return true;
        }

        return false;
    }

    @Override
    public void updated ( final Dictionary<String, ?> properties ) throws ConfigurationException
    {
        // we must not trigger changes inside the handler method
        this.executor.execute ( new Runnable () {

            @Override
            public void run ()
            {
                performUpdate ( properties );
            }
        } );
    }

    protected void performUpdate ( final Dictionary<String, ?> properties )
    {
        logger.debug ( "Update database properties: {}", properties );

        if ( properties != null )
        {
            final Hashtable<String, Object> newProps = Dictionaries.copy ( properties );

            this.properties = newProps;
            logger.debug ( "Updated database properties: {}", this.properties );
        }
        else
        {
            this.properties = null;
        }

        this.taskProvider.update ( this.properties );

        apply ( this.properties );
    }

    private void apply ( final Hashtable<String, Object> properties )
    {
        logger.debug ( "Apply database properties: {}", properties );

        try
        {
            deleteAll ();
            if ( properties != null )
            {
                createAll ( properties );
            }
        }
        catch ( IOException | InvalidSyntaxException e )
        {
            logger.warn ( "Failed to apply configuration", e );
        }
    }

    protected void createAll ( final Hashtable<String, Object> properties ) throws IOException
    {
        logger.debug ( "Create configuration: {}", properties );

        final Map<Bundle, DatabaseBundle> tracked = this.tracker.getTracked ();

        logger.debug ( "Apply to {} bundles", tracked.size () );

        for ( final DatabaseBundle db : tracked.values () )
        {
            logger.debug ( "Processing: {}", db.name );
            db.create ( properties );
        }
    }

    protected void deleteAll () throws IOException, InvalidSyntaxException
    {
        logger.debug ( "Delete all configurations" );

        final Configuration[] result = this.admin.listConfigurations ( String.format ( "(%s=%s)", "service.factoryPid", GEMINI_FACTORY_PID ) );
        if ( result != null )
        {
            for ( final Configuration cfg : result )
            {
                cfg.delete ();
            }
        }
    }
}
