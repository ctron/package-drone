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
package de.dentrassi.pm.importer.job.internal;

import java.io.InputStream;
import java.util.Map;

import de.dentrassi.osgi.job.JobInstance.Context;
import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.storage.channel.ArtifactInformation;
import de.dentrassi.pm.storage.channel.ChannelService;
import de.dentrassi.pm.storage.channel.ChannelService.By;
import de.dentrassi.pm.storage.channel.ModifiableChannel;

public class ChannelImportContext extends AbstractImportContext
{
    private final ChannelService service;

    private final String channelId;

    public ChannelImportContext ( final ChannelService service, final String channelId, final Context context )
    {
        super ( context, service, channelId );
        this.service = service;
        this.channelId = channelId;
    }

    @Override
    protected String getChannelId ()
    {
        return this.channelId;
    }

    @Override
    protected ArtifactInformation performRootImport ( final InputStream stream, final String name, final Map<MetaKey, String> providedMetaData )
    {
        return this.service.access ( By.id ( this.channelId ), ModifiableChannel.class, channel -> {
            return channel.getContext ().createArtifact ( stream, name, providedMetaData );
        } );
    }
}
