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

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.CharStreams;

public class Tasks
{
    private final static Logger logger = LoggerFactory.getLogger ( Tasks.class );

    private final TreeMap<Long, UpgradeTask> tasks = new TreeMap<> ();

    private final Bundle bundle;

    private UpgradeTask createTask;

    public Tasks ()
    {
        this.bundle = FrameworkUtil.getBundle ( Tasks.class );
        try
        {
            loadTasks ();
            loadCreateTask ();
        }
        catch ( final Exception e )
        {
            throw new RuntimeException ( e );
        }
    }

    private void loadCreateTask () throws Exception
    {
        this.createTask = loadTask ( "/sql/create.sql" );
    }

    private void loadTasks () throws Exception
    {
        final Enumeration<String> en = this.bundle.getEntryPaths ( "/sql" );

        final Pattern P = Pattern.compile ( ".*/(\\d+)_.*\\.sql" );

        while ( en.hasMoreElements () )
        {
            final String name = en.nextElement ();
            final Matcher m = P.matcher ( name );
            if ( !m.matches () )
            {
                continue;
            }

            final long nr = Long.parseLong ( m.group ( 1 ) );
            final UpgradeTask task = loadTask ( name );
            if ( task != null )
            {
                this.tasks.put ( nr, task );
            }
        }

        /*
        {
            final StatementTask s = new StatementTask ( "CREATE TABLE PROPERTIES (\"KEY\" VARCHAR(255) NOT NULL, VALUE TEXT, PRIMARY KEY(\"KEY\"))" );
            this.tasks.put ( 1L, s );
        }
        */
    }

    private UpgradeTask loadTask ( final String name ) throws Exception
    {
        final URL entry = this.bundle.getEntry ( name );

        try ( final Reader r = new InputStreamReader ( entry.openStream (), StandardCharsets.UTF_8 ) )
        {
            final String sql = CharStreams.toString ( r );
            final String[] sqlToks = sql.split ( ";" );

            final List<String> sqls = new LinkedList<> ();

            for ( final String sqlTok : sqlToks )
            {
                final String s = sqlTok.trim ();
                if ( !s.isEmpty () )
                {
                    sqls.add ( s );
                }
            }

            if ( sqls.isEmpty () )
            {
                return null;
            }

            return new StatementTask ( sqls );
        }
    }

    public void run ( final Connection connection, final UpgradeLog log, final long from, final long to ) throws SQLException
    {
        final SortedMap<Long, UpgradeTask> map = this.tasks.subMap ( from + 1, to + 1 );
        for ( final Map.Entry<Long, UpgradeTask> entry : map.entrySet () )
        {
            logger.info ( "Running schema upgrade #{}", entry.getKey () );
            entry.getValue ().run ( connection, log, entry.getKey () );
        }
    }

    public void create ( final Connection connection, final UpgradeLog log ) throws SQLException
    {
        this.createTask.run ( connection, log, null );
    }

    public Long getVersion ()
    {
        try
        {
            return this.tasks.lastKey ();
        }
        catch ( final NoSuchElementException e )
        {
            return null;
        }
    }
}
