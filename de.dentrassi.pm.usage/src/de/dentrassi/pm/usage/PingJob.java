/*******************************************************************************
 * Copyright (c) 2015 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package de.dentrassi.pm.usage;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import de.dentrassi.osgi.scheduler.ScheduledTask;
import de.dentrassi.pm.VersionInformation;
import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.core.CoreService;
import de.dentrassi.pm.storage.service.ServiceStatistics;
import de.dentrassi.pm.storage.service.StorageService;

public class PingJob implements ScheduledTask
{
    private static final MetaKey PROP_ID = new MetaKey ( "core", "random-id" );

    private static final MetaKey PROP_TS = new MetaKey ( "core", "random-id-timestamp" );

    private static final long MAX_DIFF = TimeUnit.DAYS.toMillis ( 7 );

    private StorageService storageService;

    private CoreService coreService;

    private Instant lastPing;

    public void setCoreService ( final CoreService coreService )
    {
        this.coreService = coreService;
    }

    public void setStorageService ( final StorageService storageService )
    {
        this.storageService = storageService;
    }

    @Override
    public void run () throws Exception
    {
        if ( isActive () )
        {
            new Pinger ( buildStatistics () ).start ();
            this.lastPing = Instant.now ();
        }
    }

    public Instant getLastPing ()
    {
        return this.lastPing;
    }

    private String makeId ()
    {
        final String id = this.coreService.getCoreProperty ( PROP_ID );

        if ( id == null )
        {
            return setNewId ();
        }

        Long ts = null;
        try
        {
            ts = Long.parseLong ( this.coreService.getCoreProperty ( PROP_TS ) );
        }
        catch ( final Exception e )
        {
        }

        if ( ts == null || System.currentTimeMillis () - ts > MAX_DIFF )
        {
            return setNewId ();
        }

        return id;
    }

    private String setNewId ()
    {
        final String id = UUID.randomUUID ().toString ();

        final Map<MetaKey, String> map = new HashMap<> ( 2 );
        map.put ( PROP_ID, id );
        map.put ( PROP_TS, "" + System.currentTimeMillis () );

        this.coreService.setCoreProperties ( map );

        return id;
    }

    public Statistics buildStatistics ()
    {
        final Statistics result = new Statistics ();

        final ServiceStatistics serviceStats = this.storageService.getStatistics ();

        result.setRandomId ( makeId () );
        result.setVersion ( VersionInformation.VERSION );
        result.setNumberOfArtifacts ( serviceStats.getTotalNumberOfArtifacts () );
        result.setNumberOfBytes ( serviceStats.getTotalNumberOfBytes () );

        return result;
    }

    public boolean isActive ()
    {
        return !Boolean.getBoolean ( "drone.usage.disable" );
    }
}
