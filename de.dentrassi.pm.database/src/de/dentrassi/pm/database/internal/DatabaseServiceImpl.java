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
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.service.jdbc.DataSourceFactory;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dentrassi.osgi.utils.Dictionaries;
import de.dentrassi.pm.todo.TaskProvider;

public class DatabaseServiceImpl implements ManagedService
{
    private static final String SERVICE_FACTORY_PID = "service.factoryPid";

    private static final String GEMINI_FACTORY_PID = "gemini.jpa.punit";

    private static final String PROP_UNIT_NAME = "gemini.jpa.punit.name";

    private final static Logger logger = LoggerFactory.getLogger ( DatabaseServiceImpl.class );

    public static final class DatabaseBundle
    {
        private final ConfigurationAdmin admin;

        private final String name;

        private final String filter;

        public DatabaseBundle ( final ConfigurationAdmin admin, final Bundle bundle )
        {
            this.admin = admin;
            this.name = bundle.getSymbolicName ();
            this.filter = String.format ( "(&(%s=%s)(%s=%s))", SERVICE_FACTORY_PID, GEMINI_FACTORY_PID, PROP_UNIT_NAME, this.name );
        }

        public void create ( final Hashtable<String, Object> properties ) throws IOException
        {
            logger.info ( "Creating configuration for {}: {}", this.name, properties );

            final Hashtable<String, Object> props = new Hashtable<> ( properties );

            final Configuration cfg = this.admin.createFactoryConfiguration ( GEMINI_FACTORY_PID, null );
            props.put ( PROP_UNIT_NAME, this.name );
            cfg.update ( props );
        }

        public void delete () throws Exception
        {
            logger.info ( "Deleting configuration for: {}", this.name );

            final Configuration[] result = listConfigs ();
            if ( result != null )
            {
                for ( final Configuration cfg : result )
                {
                    cfg.delete ();
                }
            }
        }

        protected Configuration[] listConfigs () throws Exception
        {
            logger.debug ( "Looking for configurations: {}", this.filter );

            final Configuration[] result = DatabaseBundle.this.admin.listConfigurations ( DatabaseBundle.this.filter );

            logger.debug ( "Looking for configurations: {} -> {}", this.filter, result );

            return result;
        }

        /**
         * Initialize the configuration <br/>
         * This will check first if the configuration is already applied to
         * reduce the number of updates
         *
         * @param the
         *            initial properties
         * @throws InvalidSyntaxException
         * @throws Exception
         */
        public void init ( Hashtable<String, Object> props ) throws Exception
        {
            logger.info ( "Initializing JPA unit configuration: {}", this.name );

            props = new Hashtable<String, Object> ( props );
            props.put ( PROP_UNIT_NAME, this.name );

            final Configuration[] result = listConfigs ();

            if ( result == null || result.length == 0 )
            {
                logger.info ( "Not configured" );
                // not configured
                create ( props );
                return;
            }

            if ( result.length > 1 )
            {
                logger.info ( "Multiple configurations. Resetting..." );
                // illegal state
                delete ();
                create ( props );
                return;
            }

            // now we must check
            final Configuration cfg = result[0];
            Dictionary<String, Object> cp = cfg.getProperties ();
            if ( cp == null )
            {
                logger.info ( "No data. Setting ... " );
                // initial update, wtf
                cfg.update ( props );
                return;
            }

            cp = Dictionaries.copy ( cp );
            cp.remove ( Constants.SERVICE_PID ); // we don't compare for this one

            // we need to add this one, in order to compare
            props.put ( SERVICE_FACTORY_PID, GEMINI_FACTORY_PID );

            // dump
            if ( logger.isDebugEnabled () )
            {
                final Set<String> keys = new HashSet<> ( Collections.list ( cp.keys () ) );
                final Set<String> masterKeys = new HashSet<> ( Collections.list ( props.keys () ) );
                for ( final String key : keys )
                {
                    logger.debug ( "\t{} -> {} / {}", key, cp.get ( key ), props.get ( key ) );
                    masterKeys.remove ( key );
                }
                for ( final String key : masterKeys )
                {
                    logger.debug ( "\t{} -> null / {}", key, props.get ( key ) );
                }
            }

            // check for size, might be match already

            if ( cp.size () != props.size () )
            {
                logger.debug ( "Size different - current: {}, master: {}", cp.size (), props.size () );
                cfg.update ( props );
                return;
            }

            // compare values

            final Set<String> currentKeys = new HashSet<> ( Collections.list ( cp.keys () ) );
            final Set<String> masterKeys = new HashSet<> ( Collections.list ( props.keys () ) );
            for ( final String currentKey : currentKeys )
            {
                if ( !props.containsKey ( currentKey ) )
                {
                    logger.info ( "Master does not contain this key: {}", currentKey );
                    cfg.update ( props );
                    return;
                }

                final Object currentValue = cp.get ( currentKey );
                final Object masterValue = props.get ( currentKey );

                // mark as processed
                masterKeys.remove ( currentKey );

                // compare
                if ( !currentValue.equals ( masterValue ) )
                {
                    logger.info ( "Value difference - {} -> current: {}, master: {}", currentKey, currentValue, masterValue );
                    cfg.update ( props );
                    return;
                }
            }

            // leftovers?
            if ( !masterKeys.isEmpty () )
            {
                logger.info ( "We have leftovers in the master: {}", masterKeys );
                cfg.update ( props );
                return;
            }

            // we are done
            logger.info ( "No update required - {}", this.name );
        }
    }

    private final BundleTrackerCustomizer<DatabaseBundle> customizer = new BundleTrackerCustomizer<DatabaseServiceImpl.DatabaseBundle> () {

        @Override
        public void removedBundle ( final Bundle bundle, final BundleEvent event, final DatabaseBundle object )
        {
            logger.trace ( "Remove bundle: {}", bundle.getSymbolicName () );
        }

        @Override
        public void modifiedBundle ( final Bundle bundle, final BundleEvent event, final DatabaseBundle object )
        {
            logger.trace ( "Modified bundle: {}", bundle.getSymbolicName () );
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

            if ( DatabaseServiceImpl.this.initialized )
            {
                try
                {
                    final Hashtable<String, Object> props = DatabaseServiceImpl.this.properties;
                    DatabaseServiceImpl.this.executor.execute ( new Runnable () {
                        @Override
                        public void run ()
                        {
                            try
                            {
                                if ( props == null )
                                {
                                    db.delete ();
                                }
                                else
                                {
                                    db.init ( props );
                                }
                            }
                            catch ( final Exception e )
                            {
                                logger.warn ( "Failed to update database configuration", e );
                            }
                        }
                    } );

                }
                catch ( final Exception e )
                {
                    logger.warn ( "Initial update failed", e );
                }
            }

            return db;
        }
    };

    private ConfigurationAdmin admin;

    private Hashtable<String, Object> properties;

    private volatile boolean initialized = false;

    private final DatabaseTaskProvider taskProvider = new DatabaseTaskProvider ();

    private final BundleContext context;

    private final BundleTracker<DatabaseBundle> tracker;

    private ServiceRegistration<?> handle;

    private ScheduledExecutorService executor;

    private final ServiceTracker<DataSourceFactory, DataSourceFactory> dataSourceTracker;

    public DatabaseServiceImpl ()
    {
        this.context = FrameworkUtil.getBundle ( DatabaseServiceImpl.class ).getBundleContext ();
        this.tracker = new BundleTracker<> ( this.context, Bundle.INSTALLED | Bundle.ACTIVE | Bundle.RESOLVED | Bundle.STARTING | Bundle.STOPPING, this.customizer );
        this.dataSourceTracker = new ServiceTracker<> ( this.context, DataSourceFactory.class, new ServiceTrackerCustomizer<DataSourceFactory, DataSourceFactory> () {

            @Override
            public DataSourceFactory addingService ( final ServiceReference<DataSourceFactory> reference )
            {
                retestSchema ();
                return null;
            }

            @Override
            public void modifiedService ( final ServiceReference<DataSourceFactory> reference, final DataSourceFactory service )
            {
            }

            @Override
            public void removedService ( final ServiceReference<DataSourceFactory> reference, final DataSourceFactory service )
            {
            }

        } );
    }

    public void setAdmin ( final ConfigurationAdmin admin )
    {
        this.admin = admin;
    }

    public void start ()
    {
        this.executor = Executors.newSingleThreadScheduledExecutor ();

        this.tracker.open ();
        {
            final Dictionary<String, Object> properties = new Hashtable<> ();
            properties.put ( Constants.SERVICE_DESCRIPTION, "Task provider for database configuration" );
            properties.put ( EventConstants.EVENT_TOPIC, new String[] { "packagedrone/database/schema" } );
            if ( this.properties != null )
            {
                this.taskProvider.update ( this.properties );
            }
            this.handle = this.context.registerService ( new String[] { TaskProvider.class.getName (), EventHandler.class.getName () }, this.taskProvider, properties );
        }

        this.dataSourceTracker.open ();
    }

    public void stop ()
    {
        if ( this.handle != null )
        {
            this.handle.unregister ();
            this.handle = null;
        }
        this.tracker.close ();
        this.dataSourceTracker.close ();

        this.executor.shutdown ();

        this.initialized = false;
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
        this.initialized = true;

        // we must not trigger changes inside the handler method
        this.executor.execute ( () -> performUpdate ( properties ) );
    }

    protected void retestSchema ()
    {
        this.executor.execute ( this.taskProvider::testSchema );
    }

    private void performUpdate ( final Dictionary<String, ?> properties )
    {
        logger.debug ( "Update database properties: {}", properties );

        if ( properties != null )
        {
            final Hashtable<String, Object> newProps = Dictionaries.copy ( properties );
            newProps.remove ( Constants.SERVICE_PID );

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
            if ( properties != null )
            {
                createAll ( properties );
            }
            else
            {
                deleteAll ();
            }
        }
        catch ( final Exception e )
        {
            logger.warn ( "Failed to apply configuration", e );
        }
    }

    private void createAll ( final Hashtable<String, Object> properties ) throws Exception
    {
        logger.debug ( "Create configuration: {}", properties );

        final Map<Bundle, DatabaseBundle> tracked = this.tracker.getTracked ();

        logger.debug ( "Apply to {} bundles", tracked.size () );

        for ( final DatabaseBundle db : tracked.values () )
        {
            logger.debug ( "Processing: {}", db.name );
            db.init ( properties );
        }
    }

    private void deleteAll () throws IOException, InvalidSyntaxException
    {
        logger.debug ( "Delete all configurations" );

        final Configuration[] result = this.admin.listConfigurations ( String.format ( "(%s=%s)", SERVICE_FACTORY_PID, GEMINI_FACTORY_PID ) );
        if ( result != null )
        {
            for ( final Configuration cfg : result )
            {
                cfg.delete ();
            }
        }
    }
}
