/*******************************************************************************
 * Copyright (c) 2014 Jens Reimann.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jens Reimann - initial API and implementation
 *******************************************************************************/
package de.dentrassi.pm.database;

import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.jdbc.DataSourceFactory;
import org.osgi.util.tracker.ServiceTracker;

public class DatabaseSetup implements AutoCloseable
{
    public static final String KEY_DATABASE_SCHEMA_VERSION = "database-schema-version";

    private final DatabaseConnectionData data;

    private final ServiceTracker<DataSourceFactory, DataSourceFactory> tracker;

    private final List<String> init = new LinkedList<> ();

    private final Tasks tasks = new Tasks ();

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

        this.tracker = new ServiceTracker<DataSourceFactory, DataSourceFactory> ( FrameworkUtil.getBundle ( DatabaseSetup.class ).getBundleContext (), filter, null );
        this.tracker.open ();

        // init sql

        if ( "com.mysql.jdbc.Driver".equals ( data.getJdbcDriver () ) )
        {
            this.init.add ( "SET SESSION sql_mode = 'ANSI'" );
        }
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
        return doWithConnection ( ( con ) -> loadSchemaVersion ( con ) );
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
            try ( final PreparedStatement stmt = con.prepareStatement ( "select VALUE from PROPERTIES where \"KEY\"=?" ) )
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
        }
        catch ( final SQLException e )
        {
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
        }
        return p;
    }

    @Override
    public void close ()
    {
        this.tracker.close ();
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

        return log;
    }

    public boolean isConfigured ()
    {
        return this.data.getJdbcDriver () != null && !this.data.getJdbcDriver ().isEmpty ();
    }

    public boolean isNeedUpgrade ()
    {
        if ( isConfigured () && ( getSchemaVersion () == null || getCurrentVersion () > getSchemaVersion () ) )
        {
            return true;
        }
        else
        {
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
        try
        {
            return doWithConnection ( ( t ) -> null, 5 );
        }
        catch ( final Exception e )
        {
            return e;
        }
    }
}
