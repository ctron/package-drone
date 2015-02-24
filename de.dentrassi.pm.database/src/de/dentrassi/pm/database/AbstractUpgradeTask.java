/*******************************************************************************
 * Copyright (c) 2014 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package de.dentrassi.pm.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public abstract class AbstractUpgradeTask implements UpgradeTask
{
    @Override
    public void run ( final Connection connection, final UpgradeLog log, final Long version ) throws SQLException
    {
        performRun ( connection, log );
        commitVersion ( connection, version );
    }

    protected abstract void performRun ( Connection connection, UpgradeLog log ) throws SQLException;

    protected void commitVersion ( final Connection connection, final Long version ) throws SQLException
    {
        if ( version == null )
        {
            connection.commit ();
            return;
        }

        if ( executeUpgrade ( connection, "UPDATE PROPERTIES SET VALUE=? WHERE \"KEY\"=?", "" + version, DatabaseSetup.KEY_DATABASE_SCHEMA_VERSION ) < 1 )
        {
            executeUpgrade ( connection, "INSERT INTO PROPERTIES (\"KEY\", VALUE) values (?,?)", DatabaseSetup.KEY_DATABASE_SCHEMA_VERSION, "" + version );
        }
        connection.commit ();
    }

    protected int executeUpgrade ( final Connection connection, final String sql, final Object... values ) throws SQLException
    {
        try ( final PreparedStatement stmt = connection.prepareStatement ( sql ) )
        {
            for ( int i = 0; i < values.length; i++ )
            {
                stmt.setObject ( i + 1, values[i] );
            }
            return stmt.executeUpdate ();
        }
    }

}
