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
    private final DatabaseConnectionData data;

    private final ServiceTracker<DataSourceFactory, DataSourceFactory> tracker;

    private final long currentVersion = 1;

    private final List<String> init = new LinkedList<> ();

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

        if ( data.getJdbcDriver ().equals ( "com.mysql.jdbc.Driver" ) )
        {
            this.init.add ( "SET SESSION sql_mode = 'ANSI'" );
        }
    }

    public long getCurrentVersion ()
    {
        return this.currentVersion;
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
        final DataSourceFactory factory = this.tracker.getService ();
        if ( factory == null )
        {
            throw new IllegalStateException ( String.format ( "Unable to find data source factory for driver '%s'", this.data.getJdbcDriver () ) );
        }

        try
        {
            final Properties props = new Properties ();

            props.put ( DataSourceFactory.JDBC_PASSWORD, this.data.getPassword () );
            props.put ( DataSourceFactory.JDBC_USER, this.data.getUser () );
            props.put ( DataSourceFactory.JDBC_URL, this.data.getUrl () );

            props.putAll ( makeAdditional ( this.data.getAdditionalProperties () ) );

            final DataSource ds = factory.createDataSource ( props );
            try ( Connection con = ds.getConnection () )
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
                stmt.setString ( 1, "database-schema-version" );
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

        doWithConnection ( connection -> {
            Long from = loadSchemaVersion ( connection );
            if ( from == null )
            {
                from = -1L;
            }
            new Tasks ().run ( connection, log, from, this.currentVersion );
            return null;
        } );

        return log;
    }
}
