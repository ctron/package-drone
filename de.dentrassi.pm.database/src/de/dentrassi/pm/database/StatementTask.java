package de.dentrassi.pm.database;

import java.sql.Connection;
import java.sql.SQLException;
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
