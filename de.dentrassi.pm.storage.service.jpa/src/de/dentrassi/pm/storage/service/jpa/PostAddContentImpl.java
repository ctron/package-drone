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
package de.dentrassi.pm.storage.service.jpa;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import de.dentrassi.pm.aspect.listener.PostAddContext;
import de.dentrassi.pm.common.ArtifactInformation;
import de.dentrassi.pm.common.MetaKey;

public class PostAddContentImpl implements PostAddContext
{

    private final StorageHandlerImpl storageHandler;

    private final String channelId;

    private SortedMap<MetaKey, String> metadata;

    private Set<ArtifactInformation> artifacts;

    public PostAddContentImpl ( final StorageHandlerImpl storageHandler, final String channelId )
    {
        this.storageHandler = storageHandler;
        this.channelId = channelId;
    }

    @Override
    public Collection<ArtifactInformation> getChannelArtifacts ()
    {
        if ( this.artifacts == null )
        {
            this.artifacts = this.storageHandler.getArtifacts ( this.channelId );
        }
        return this.artifacts;
    }

    @Override
    public Map<MetaKey, String> getChannelMetaData ()
    {
        if ( this.metadata == null )
        {
            this.metadata = this.storageHandler.getChannelMetaData ( this.channelId );
        }
        return this.metadata;
    }

    @Override
    public void deleteArtifact ( final String artifactId )
    {
        this.storageHandler.deleteArtifact ( artifactId );
    }

}
