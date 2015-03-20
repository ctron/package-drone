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
package de.dentrassi.pm.database;

import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.sql.DataSource;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.jdbc.DataSourceFactory;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dentrassi.pm.database.schema.Tasks;
import de.dentrassi.pm.database.schema.UpgradeLog;

public class DatabaseSetup implements AutoCloseable
{
    private final static Logger logger = LoggerFactory.getLogger ( DatabaseSetup.class );

    public static final String KEY_DATABASE_SCHEMA_VERSION = "database-schema-version";

    private final DatabaseConnectionData data;

    private final ServiceTracker<DataSourceFactory, DataSourceFactory> tracker;

    private final ServiceTracker<EventAdmin, EventAdmin> eventAdminTracker;

    private final List<String> init = new LinkedList<> ();

    private final Tasks tasks;

    public DatabaseSetup ( final DatabaseConnectionData data )
    {
        this.data = data;
        Filter filter;
        try
        {
            filter = FrameworkUtil.createFilter ( String.format ( "(&(%s=%s)(osgi.jdbc.driver.class=%s))", Constants.OBJECTCLASS, DataSourceFactory.class.getName (), data.getJdbcDriver () ) );
        }
        catch ( final InvalidSyntaxException e )
        {
            throw new IllegalStateException ( "Failed to create filter for JDBC driver factory", e );
        }

        final BundleContext context = FrameworkUtil.getBundle ( DatabaseSetup.class ).getBundleContext ();

        this.tracker = new ServiceTracker<DataSourceFactory, DataSourceFactory> ( context, filter, null );
        this.tracker.open ();

        this.eventAdminTracker = new ServiceTracker<> ( context, EventAdmin.class, null );
        this.eventAdminTracker.open ();

        // init sql

        final Set<String> defines = new HashSet<> ();

        if ( "com.mysql.jdbc.Driver".equals ( data.getJdbcDriver () ) )
        {
            this.init.add ( "SET SESSION sql_mode = 'ANSI'" );
            defines.add ( "mysql" );
        }
        else if ( "org.postgresql.Driver".equals ( data.getJdbcDriver () ) )
        {
            defines.add ( "postgres" );
        }

        this.tasks = new Tasks ( defines );
    }

    @Override
    public void close ()
    {
        this.tracker.close ();
        this.eventAdminTracker.close ();
    }

    public long getCurrentVersion ()
    {
        final Long v = this.tasks.getVersion ();
        if ( v == null )
        {
            return -1;
        }
        else
        {
            return v;
        }
    }

    public Long getSchemaVersion ()
    {
        try
        {
            return doWithConnection ( ( con ) -> loadSchemaVersion ( con ) );
        }
        catch ( final Exception e )
        {
            return null;
        }
    }

    @FunctionalInterface
    public interface SqlFunction<T>
    {
        public T apply ( Connection connection ) throws SQLException;
    }

    public <T> T doWithConnection ( final SqlFunction<T> func )
    {
        return doWithConnection ( func, null );
    }

    public <T> T doWithConnection ( final SqlFunction<T> func, final Integer login )
    {
        final DataSourceFactory factory = this.tracker.getService ();
        if ( factory == null )
        {
            throw new IllegalStateException ( String.format ( "Unable to find data source factory for driver '%s'", this.data.getJdbcDriver () ) );
        }

        try
        {
            final Properties props = new Properties ();

            props.put ( DataSourceFactory.JDBC_URL, this.data.getUrl () );

            props.putAll ( makeAdditional ( this.data.getAdditionalProperties () ) );

            final DataSource ds = factory.createDataSource ( props );

            // TODO: since we use an URL based connection, Gemini does not allow us to set the login timeout
            /*
            if ( login != null )
            {
                ds.setLoginTimeout ( login );
            }
            */

            try ( Connection con = ds.getConnection ( this.data.getUser (), this.data.getPassword () ) )
            {
                initConnection ( con );
                return func.apply ( con );
            }
        }
        catch ( final Exception e )
        {
            logger.warn ( "Failed to process", e );
            throw new RuntimeException ( e );
        }
    }

    protected void initConnection ( final Connection connection ) throws SQLException
    {
        connection.setAutoCommit ( false );
        for ( final String init : this.init )
        {
            try ( PreparedStatement stmt = connection.prepareStatement ( init ) )
            {
                stmt.execute ();
            }
        }
    }

    private Long loadSchemaVersion ( final Connection con )
    {
        try
        {
            con.setAutoCommit ( true ); // temporarily enable autocommit to prevent failure in next transaction if this statement fails
            try ( final PreparedStatement stmt = con.prepareStatement ( "select \"VALUE\" from PROPERTIES where \"KEY\"=?" ) )
            {
                stmt.setString ( 1, KEY_DATABASE_SCHEMA_VERSION );
                try ( final ResultSet rs = stmt.executeQuery () )
                {
                    if ( !rs.next () )
                    {
                        return null;
                    }
                    final String str = rs.getString ( 1 );
                    try
                    {
                        return Long.parseLong ( str );
                    }
                    catch ( final NumberFormatException e )
                    {
                        return null;
                    }
                }
            }
            finally
            {
                con.setAutoCommit ( false );
            }
        }
        catch ( final SQLException e )
        {
            logger.info ( "Failed to check schema version", e );
            return null;
        }
    }

    private Map<? extends Object, ? extends Object> makeAdditional ( final String additionalProperties )
    {
        final Properties p = new Properties ();
        try
        {
            p.load ( new StringReader ( additionalProperties ) );
        }
        catch ( final IOException e )
        {
            logger.warn ( "Failed to parse additional properties", e );
        }
        return p;
    }

    public UpgradeLog performUpgrade ()
    {
        final UpgradeLog log = new UpgradeLog ();

        final long l = getCurrentVersion ();
        if ( l < 0 )
        {
            // we don't have anything in the bundle
            return log;
        }

        doWithConnection ( connection -> {
            final Long from = loadSchemaVersion ( connection );
            if ( from == null )
            {
                this.tasks.create ( connection, log );
            }
            else
            {
                this.tasks.run ( connection, log, from, l );
            }
            return null;
        } );

        final EventAdmin eventAdmin = this.eventAdminTracker.getService ();
        if ( eventAdmin != null )
        {
            eventAdmin.sendEvent ( new Event ( "packagedrone/database/schema", Collections.emptyMap () ) );
        }

        return log;
    }

    public boolean isConfigured ()
    {
        if ( this.data.getJdbcDriver () == null || this.data.getJdbcDriver ().isEmpty () )
        {
            return false;
        }
        return true;
    }

    public boolean isNeedUpgrade ()
    {
        try
        {
            final Long schemaVersion = getSchemaVersion ();
            if ( isConfigured () && ( schemaVersion == null || getCurrentVersion () > schemaVersion ) )
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        catch ( final Exception e )
        {
            logger.warn ( "Failed to evaluate needUpgrade", e );
            return false;
        }
    }

    /**
     * Test if the database connection is valid <br/>
     *
     * @return the connection error or <code>null</code>
     */
    public Exception testConnection ()
    {
        if ( !isConfigured () )
        {
            return null;
        }

        try
        {
            return doWithConnection ( ( t ) -> null, 5 );
        }
        catch ( final Exception e )
        {
            logger.warn ( "Failed to test connection", e );
            return e;
        }
    }
}
