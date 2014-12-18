package de.dentrassi.pm.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class Tasks
{
    private final TreeMap<Long, UpgradeTask> tasks = new TreeMap<> ();

    public Tasks ()
    {
        loadTasks ();
    }

    private void loadTasks ()
    {
        {
            final StatementTask s = new StatementTask ( "CREATE TABLE PROPERTIES (\"KEY\" VARCHAR(255) NOT NULL, VALUE TEXT, PRIMARY KEY(\"KEY\"))" );
            this.tasks.put ( 1L, s );
        }
    }

    public void run ( final Connection connection, final UpgradeLog log, final long from, final long to ) throws SQLException
    {
        final SortedMap<Long, UpgradeTask> map = this.tasks.subMap ( from + 1, to + 1 );
        for ( final Map.Entry<Long, UpgradeTask> entry : map.entrySet () )
        {
            entry.getValue ().run ( connection, log, entry.getKey () );
        }
    }
}
