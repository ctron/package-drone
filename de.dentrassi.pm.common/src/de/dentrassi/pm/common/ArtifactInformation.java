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
package de.dentrassi.pm.common;

import java.util.Collections;
import java.util.SortedMap;

public class ArtifactInformation implements Comparable<ArtifactInformation>
{
    private final long size;

    private final String name;

    private final String channelId;

    private final SortedMap<MetaKey, String> metaData;

    private final String id;

    private final String parentId;

    public ArtifactInformation ( final String id, final String parentId, final long size, final String name, final String channelId, final SortedMap<MetaKey, String> metadata )
    {
        this.id = id;
        this.size = size;
        this.name = name;
        this.channelId = channelId;
        this.metaData = Collections.unmodifiableSortedMap ( metadata );
        this.parentId = parentId;
    }

    public String getParentId ()
    {
        return this.parentId;
    }

    public String getId ()
    {
        return this.id;
    }

    @Deprecated
    public long getLength ()
    {
        return this.size;
    }

    public long getSize ()
    {
        return this.size;
    }

    public String getName ()
    {
        return this.name;
    }

    public String getChannelId ()
    {
        return this.channelId;
    }

    public SortedMap<MetaKey, String> getMetaData ()
    {
        return this.metaData;
    }

    @Override
    public int compareTo ( final ArtifactInformation o )
    {
        return this.id.compareTo ( o.id );
    }
}
