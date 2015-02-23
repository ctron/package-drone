/*******************************************************************************
 * Copyright (c) 2015 IBH SYSTEMS GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package de.dentrassi.pm.database.internal;

import java.util.Dictionary;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dentrassi.osgi.web.LinkTarget;
import de.dentrassi.pm.database.DatabaseConnectionData;
import de.dentrassi.pm.database.DatabaseSetup;
import de.dentrassi.pm.todo.BasicTask;
import de.dentrassi.pm.todo.DefaultTaskProvider;
import de.dentrassi.pm.todo.Task.State;

public class DatabaseTaskProvider extends DefaultTaskProvider implements EventHandler
{

    private final static Logger logger = LoggerFactory.getLogger ( DatabaseTaskProvider.class );

    private final BasicTask mainTask;

    private Dictionary<String, Object> properties;

    private BasicTask schemaTask;

    public DatabaseTaskProvider ()
    {
        this.mainTask = new BasicTask ( "Configure the database connection", 2, "Head over to the <q>Database configuration</q> section and enter your database settings. Be sure you have a database instance set up.", new LinkTarget ( "/config" ) );
        addTask ( this.mainTask );
    }

    public void update ( final Dictionary<String, Object> properties )
    {
        this.mainTask.setState ( properties == null ? State.TODO : State.DONE );

        this.properties = properties;

        testSchema ();

        fireNotify ();
    }

    @Override
    public void handleEvent ( final Event event )
    {
        logger.debug ( "Event: {}", event.getTopic () );

        if ( event.getTopic ().equals ( "packagedrone/database/schema" ) )
        {
            testSchema ();
        }
    }

    public void testSchema ()
    {
        try ( DatabaseSetup db = new DatabaseSetup ( DatabaseConnectionData.fromProperties ( this.properties ) ) )
        {
            final Long active = db.getSchemaVersion ();

            if ( active == null )
            {
                setSchemaState ( "Install database schema", "Install the schema" );
            }
            else if ( active != null && active < db.getCurrentVersion () )
            {
                setSchemaState ( "Upgrade database schema", String.format ( "Upgrade from version %s to version %s", active, db.getCurrentVersion () ) );
            }
            else
            {
                setSchemaState ( null, null );
            }
        }
        catch ( final Exception e )
        {
            setSchemaState ( null, null );
        }
    }

    private void setSchemaState ( final String message, final String description )
    {
        if ( this.schemaTask != null )
        {
            removeTask ( this.schemaTask );
            this.schemaTask = null;
        }

        if ( message != null )
        {
            this.schemaTask = new BasicTask ( message, 3, description, new LinkTarget ( "/config" ) );
            addTask ( this.schemaTask );
        }
    }
}
