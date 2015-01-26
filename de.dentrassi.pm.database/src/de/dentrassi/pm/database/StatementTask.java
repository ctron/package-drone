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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class StatementTask extends AbstractUpgradeTask
{
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
        for ( final String sql : this.sqls )
        {
            executeUpgrade ( connection, sql );
        }
    }
}
