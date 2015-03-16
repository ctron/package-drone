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
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatementTask extends AbstractUpgradeTask
{
    private final static Logger logger = LoggerFactory.getLogger ( StatementTask.class );

    private final List<String> sqls = new LinkedList<> ();

    public StatementTask ()
    {
    }

    public StatementTask ( final String sql )
    {
        this.sqls.add ( sql );
    }

    public StatementTask ( final String[] sqls )
    {
        this.sqls.addAll ( Arrays.asList ( sqls ) );
    }

    public StatementTask ( final Collection<String> sqls )
    {
        this.sqls.addAll ( new LinkedList<> ( sqls ) );
    }

    public void add ( final String sql )
    {
        this.sqls.add ( sql );
    }

    @Override
    protected void performRun ( final Connection connection, final UpgradeLog log ) throws SQLException
    {
        for ( String sql : this.sqls )
        {
            sql = performSql ( connection, sql );
        }
    }

    protected String performSql ( final Connection connection, String sql ) throws SQLException
    {
        boolean ignoreError = false;

        if ( sql.startsWith ( "@" ) )
        {
            ignoreError = true;
            sql = sql.substring ( 1 );
        }

        if ( ignoreError )
        {
            try
            {
                executeUpgrade ( connection, sql );
            }
            catch ( final SQLException e )
            {
                logger.warn ( "Upgrade failed, but it can be ignored", e );
            }
        }
        else
        {
            executeUpgrade ( connection, sql );
        }
        return sql;
    }
}
