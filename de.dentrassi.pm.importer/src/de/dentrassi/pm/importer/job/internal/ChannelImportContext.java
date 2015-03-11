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
import de.dentrassi.pm.storage.Artifact;
import de.dentrassi.pm.storage.Channel;
import de.dentrassi.pm.storage.service.StorageService;

public class ChannelImportContext extends AbstractImportContext
{
    private final Channel channel;

    public ChannelImportContext ( final StorageService service, final String channelId, final Context context )
    {
        super ( context );
        this.channel = service.getChannel ( channelId );
        if ( this.channel == null )
        {
            throw new IllegalArgumentException ( String.format ( "Channel '%s' cannot be found", channelId ) );
        }
    }

    @Override
    protected String getChannelId ()
    {
        return this.channel.getId ();
    }

    @Override
    protected Artifact performRootImport ( final InputStream stream, final String name, final Map<MetaKey, String> providedMetaData )
    {
        return this.channel.createArtifact ( name, stream, providedMetaData );
    }
}
