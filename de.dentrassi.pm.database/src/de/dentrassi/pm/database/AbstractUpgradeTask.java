package de.dentrassi.pm.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public abstract class AbstractUpgradeTask implements UpgradeTask
{
    @Override
    public void run ( final Connection connection, final UpgradeLog log, final long version ) throws SQLException
    {
        performRun ( connection, log );
        commitVersion ( connection, version );
    }

    protected abstract void performRun ( Connection connection, UpgradeLog log ) throws SQLException;

    protected void commitVersion ( final Connection connection, final long version ) throws SQLException
    {
        if ( executeUpgrade ( connection, "UPDATE PROPERTIES SET VALUE=? WHERE \"KEY\"=?", "" + version, "database-schema-version" ) < 1 )
        {
            executeUpgrade ( connection, "INSERT INTO PROPERTIES (\"KEY\", VALUE) values (?,?)", "database-schema-version", "" + version );
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
