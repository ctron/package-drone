/*******************************************************************************
 * Copyright (c) 2014, 2015 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package de.dentrassi.pm.database.schema;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Tasks
{
    private final static Logger logger = LoggerFactory.getLogger ( Tasks.class );

    private final TreeMap<Long, UpgradeTask> tasks = new TreeMap<> ();

    private final Bundle bundle;

    private UpgradeTask createTask;

    private final Set<String> defines;

    public Tasks ( final Set<String> defines )
    {
        this.defines = defines;
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
        this.createTask = createParser ().loadTask ( "/sql/create.sql" );
    }

    private Parser createParser ()
    {
        return new Parser ( this.bundle, this.defines );
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
            final UpgradeTask task = createParser ().loadTask ( name );
            if ( task != null )
            {
                this.tasks.put ( nr, task );
            }
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
