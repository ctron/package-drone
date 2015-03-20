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
package de.dentrassi.pm.aspect.upgrade;

import java.util.Collection;

import de.dentrassi.osgi.job.JobFactory;
import de.dentrassi.osgi.job.JobFactoryDescriptor;
import de.dentrassi.osgi.job.JobInstance;
import de.dentrassi.osgi.job.JobInstance.Context;
import de.dentrassi.osgi.web.LinkTarget;
import de.dentrassi.pm.storage.Channel;
import de.dentrassi.pm.storage.service.StorageService;

public class UpgradeAllChannelsJob implements JobFactory
{

    private static final JobFactoryDescriptor DESCRIPTOR = new JobFactoryDescriptor () {

        @Override
        public LinkTarget getResultTarget ()
        {
            return null;
        }
    };

    public static final String ID = "drone.aspect.refreshAllChannels";

    private StorageService service;

    public void setService ( final StorageService service )
    {
        this.service = service;
    }

    @Override
    public JobFactoryDescriptor getDescriptor ()
    {
        return DESCRIPTOR;
    }

    @Override
    public JobInstance createInstance ( final String data ) throws Exception
    {
        return ( ctx ) -> {
            process ( ctx );
        };
    }

    private void process ( final Context ctx )
    {
        final Collection<Channel> channels = this.service.listChannels ();
        ctx.beginWork ( "Refreshing channels", channels.size () );

        for ( final Channel channel : channels )
        {
            ctx.setCurrentTaskName ( String.format ( "Processing %s", channel.getNameAndId () ) );
            this.service.refreshAllChannelAspects ( channel.getId () );
            ctx.worked ( 1 );
        }

        ctx.complete ();
    }

    @Override
    public String encodeConfiguration ( final Object data )
    {
        return null;
    }

    @Override
    public String makeLabel ( final String data )
    {
        return "Reprocess all channels";
    }

}
