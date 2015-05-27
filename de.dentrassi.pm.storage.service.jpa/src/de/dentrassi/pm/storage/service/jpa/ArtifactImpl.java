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

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import de.dentrassi.pm.common.ArtifactInformation;
import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.storage.Artifact;
import de.dentrassi.pm.storage.ArtifactReceiver;
import de.dentrassi.pm.storage.Channel;
import de.dentrassi.pm.storage.ValidationMessage;

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
    public List<ValidationMessage> getValidationMessages ()
    {
        return this.channel.getValidationMessagesForArtifact ( this.id );
    }

    @Override
    public Artifact attachArtifact ( final String name, final InputStream stream, final Map<MetaKey, String> providedMetaData )
    {
        return this.channel.getService ().createAttachedArtifact ( this.channel.getId (), this.id, name, stream, providedMetaData );
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
        try
        {
            this.channel.streamData ( this.id, consumer );
        }
        catch ( final FileNotFoundException e )
        {
            throw new IllegalStateException ( e );
        }
    }

    @Override
    public void applyMetaData ( final Map<MetaKey, String> metadata )
    {
        this.channel.getService ().applyMetaData ( this.id, metadata );
    }
}
