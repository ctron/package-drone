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

import java.util.Comparator;
import java.util.Date;

public class SimpleArtifactInformation implements Comparable<SimpleArtifactInformation>
{

    private final long size;

    private final String name;

    private final String channelId;

    private final String id;

    private final String parentId;

    private final Date creationTimestamp;

    private final boolean derived;

    public SimpleArtifactInformation ( final String id, final String parentId, final long size, final String name, final String channelId, final Date creationTimestamp, final boolean derived )
    {
        this.id = id;
        this.size = size;
        this.name = name;
        this.channelId = channelId;
        this.parentId = parentId;
        this.creationTimestamp = creationTimestamp;
        this.derived = derived;
    }

    public boolean isDerived ()
    {
        return this.derived;
    }

    public Date getCreationTimestamp ()
    {
        return this.creationTimestamp;
    }

    public String getParentId ()
    {
        return this.parentId;
    }

    public String getId ()
    {
        return this.id;
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

    @Override
    public int compareTo ( final SimpleArtifactInformation o )
    {
        return this.id.compareTo ( o.id );
    }

    public static Comparator<SimpleArtifactInformation> NAME_COMPARATOR = new Comparator<SimpleArtifactInformation> () {

        @Override
        public int compare ( final SimpleArtifactInformation o1, final SimpleArtifactInformation o2 )
        {
            final int result = o1.getName ().compareTo ( o2.getName () );
            if ( result != 0 )
            {
                return result;
            }
            return o1.getId ().compareTo ( o2.getId () );
        }
    };

}
