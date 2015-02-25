/*******************************************************************************
 * Copyright (c) 2014, 2015 IBH SYSTEMS GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package de.dentrassi.pm.setup.web;

import java.io.IOException;
import java.io.StringReader;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dentrassi.pm.database.DatabaseConfigurationService;
import de.dentrassi.pm.database.DatabaseConnectionData;

public class Configurator implements AutoCloseable
{
    private final static Logger logger = LoggerFactory.getLogger ( Configurator.class );

    private final ServiceTracker<ConfigurationAdmin, ConfigurationAdmin> tracker;

    public static Configurator create ()
    {
        return new Configurator ( FrameworkUtil.getBundle ( Configurator.class ).getBundleContext () );
    }

    public Configurator ( final BundleContext context )
    {
        this.tracker = new ServiceTracker<> ( context, ConfigurationAdmin.class, null );
        this.tracker.open ();
    }

    @Override
    public void close ()
    {
        this.tracker.close ();
    }

    public void setDatabaseSettings ( final DatabaseConnectionData data )
    {
        final ConfigurationAdmin cm = this.tracker.getService ();
        if ( cm == null )
        {
            throw new IllegalStateException ( String.format ( "Configuration Admin not found" ) );
        }

        try
        {
            // set main db configuration

            Configuration dbConf = cm.getConfiguration ( DatabaseConfigurationService.ID, null );

            final Dictionary<String, Object> props = makeProperties ( data );

            if ( Boolean.getBoolean ( "package.drone.database.forceUpdate" ) )
            {
                logger.debug ( "Forcing update" );
                dbConf.delete ();
                dbConf = cm.getConfiguration ( DatabaseConfigurationService.ID, null );
            }

            logger.debug ( "Setting new database properties: {}", props );
            dbConf.update ( props );
        }
        catch ( final Exception e )
        {
            logger.warn ( "Failed to set database settings", e );
            throw new RuntimeException ( e );
        }
    }

    protected Dictionary<String, Object> makeProperties ( final DatabaseConnectionData data ) throws IOException
    {
        final Dictionary<String, Object> props = new Hashtable<> ();

        props.put ( "javax.persistence.jdbc.driver", data.getJdbcDriver () );
        props.put ( "javax.persistence.jdbc.url", data.getUrl () );
        props.put ( "javax.persistence.jdbc.user", data.getUser () );
        props.put ( "javax.persistence.jdbc.password", data.getPassword () );

        final Properties p = new Properties ();
        p.load ( new StringReader ( data.getAdditionalProperties () ) );

        for ( final Map.Entry<Object, Object> entry : p.entrySet () )
        {
            props.put ( "javax.persistence.jdbc." + entry.getKey (), entry.getValue () );
        }
        return props;
    }

    public DatabaseConnectionData getDatabaseSettings ()
    {
        final ConfigurationAdmin cm = this.tracker.getService ();
        if ( cm == null )
        {
            return null;
        }

        try
        {
            final Configuration cfg = cm.getConfiguration ( DatabaseConfigurationService.ID, null );
            if ( cfg == null || cfg.getProperties () == null || cfg.getProperties ().isEmpty () )
            {
                return new DatabaseConnectionData ();
            }

            return DatabaseConnectionData.fromProperties ( cfg.getProperties () );
        }
        catch ( final IOException e )
        {
            throw new RuntimeException ( e );
        }
    }

}
