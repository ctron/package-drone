/*******************************************************************************
 * Copyright (c) 2014, 2015 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package de.dentrassi.pm.storage.service.jpa;

import javax.persistence.EntityManager;

import de.dentrassi.pm.storage.jpa.ArtifactEntity;
import de.dentrassi.pm.storage.jpa.ChannelEntity;

public abstract class AbstractHandler
{
    protected final EntityManager em;

    public AbstractHandler ( final EntityManager em )
    {
        this.em = em;
    }

    protected ChannelEntity getCheckedChannel ( final String channelId )
    {
        final ChannelEntity channel = this.em.find ( ChannelEntity.class, channelId );
        if ( channel == null )
        {
            throw new IllegalArgumentException ( String.format ( "Channel %s unknown", channelId ) );
        }
        return channel;
    }

    protected ArtifactEntity getCheckedArtifact ( final String artifactId )
    {
        final ArtifactEntity artifact = this.em.find ( ArtifactEntity.class, artifactId );
        if ( artifact == null )
        {
            throw new IllegalArgumentException ( String.format ( "Artifact %s unknown", artifactId ) );
        }
        return artifact;
    }

}
