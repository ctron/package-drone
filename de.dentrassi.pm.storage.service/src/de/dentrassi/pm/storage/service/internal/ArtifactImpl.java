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
package de.dentrassi.pm.storage.service.internal;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import de.dentrassi.pm.storage.service.Artifact;
import de.dentrassi.pm.storage.service.ArtifactReceiver;
import de.dentrassi.pm.storage.service.Channel;
import de.dentrassi.pm.storage.service.MetaKey;

public class ArtifactImpl implements Artifact
{

    private final String id;

    private final ChannelImpl channel;

    private final long size;

    private final String name;

    private final SortedMap<MetaKey, String> metaData;

    public ArtifactImpl ( final ChannelImpl channel, final String id, final String name, final long size, final Map<MetaKey, String> metaData )
    {
        this.id = id;
        this.channel = channel;
        this.name = name;
        this.size = size;
        this.metaData = new TreeMap<MetaKey, String> ( metaData );
    }

    @Override
    public String getName ()
    {
        return this.name;
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
    public long getSize ()
    {
        return this.size;
    }

    @Override
    public void streamData ( final ArtifactReceiver consumer )
    {
        this.channel.streamData ( this.id, consumer );
    }

    @Override
    public SortedMap<MetaKey, String> getMetaData ()
    {
        return this.metaData;
    }

    @Override
    public int compareTo ( final Artifact o )
    {
        if ( o == null )
        {
            return 1;
        }

        return this.id.compareTo ( o.getId () );
    }

    @Override
    public void applyMetaData ( final Map<MetaKey, String> metadata )
    {
        this.channel.getService ().applyMetaData ( this.id, metadata );
    }

}
