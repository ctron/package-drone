/*******************************************************************************
 * Copyright (c) 2014 Jens Reimann.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jens Reimann - initial API and implementation
 *******************************************************************************/
package de.dentrassi.pm.storage.service.jpa;

import java.util.Map;

import de.dentrassi.pm.common.ArtifactInformation;
import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.storage.Artifact;
import de.dentrassi.pm.storage.ArtifactReceiver;
import de.dentrassi.pm.storage.Channel;

public class ArtifactImpl implements Artifact
{
    protected final String id;

    protected final ChannelImpl channel;

    private final ArtifactInformation information;

    public ArtifactImpl ( final ChannelImpl channel, final String id, final ArtifactInformation information )
    {
        this.id = id;
        this.channel = channel;
        this.information = information;
    }

    @Override
    public ArtifactInformation getInformation ()
    {
        return this.information;
    }

    @Override
    public Artifact getParent ()
    {
        if ( this.information.getParentId () == null )
        {
            return null;
        }
        else
        {
            return this.channel.getService ().getArtifact ( this.information.getParentId () );
        }
    }

    @Override
    public Channel getChannel ()
    {
        return this.channel;
    }

    @Override
    public String getId ()
    {
        return this.id;
    }

    @Override
    public void streamData ( final ArtifactReceiver consumer )
    {
        this.channel.streamData ( this.id, consumer );
    }

    @Override
    public void applyMetaData ( final Map<MetaKey, String> metadata )
    {
        this.channel.getService ().applyMetaData ( this.id, metadata );
    }
}
