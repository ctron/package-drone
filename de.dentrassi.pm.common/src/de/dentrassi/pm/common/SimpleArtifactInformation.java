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
package de.dentrassi.pm.common;

import java.util.Comparator;
import java.util.Date;
import java.util.Set;

/**
 * Basic artifact information
 * <p>
 * This class only provides direct information on an artifact, which can be
 * retrieved without join to much tables in the database.
 * </p>
 */
public class SimpleArtifactInformation implements Comparable<SimpleArtifactInformation>
{
    /**
     * The size of the artifact data in bytes
     */
    private final long size;

    /**
     * The name of the artifact
     * <p>
     * Artifact names are not unique. Neither globally nor in a channel.
     * </p>
     */
    private final String name;

    /**
     * The ID of the channel this artifact is stored in
     */
    private final String channelId;

    /**
     * The ID of the artifact
     */
    private final String id;

    /**
     * The ID of the containing artifact
     * <p>
     * If the artifact is not child element, then this value will be
     * <code>null</code>.
     * </p>
     */
    private final String parentId;

    /**
     * The timestamp when this artifact was stored in the repository
     */
    private final Date creationTimestamp;

    /**
     * The different facets of the artifact
     * <p>
     * This can a list of tags like
     * <q>deletable</q>, which can be used to adapt the UI based on the
     * capabilities of this artifacts
     * </p>
     */
    private final Set<String> facets;

    public SimpleArtifactInformation ( final String id, final String parentId, final long size, final String name, final String channelId, final Date creationTimestamp, final Set<String> facets )
    {
        this.id = id;
        this.size = size;
        this.name = name;
        this.channelId = channelId;
        this.parentId = parentId;
        this.creationTimestamp = creationTimestamp;
        this.facets = facets;
    }

    public Set<String> getFacets ()
    {
        return this.facets;
    }

    public boolean is ( final String facet )
    {
        return this.facets.contains ( facet );
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

    /**
     * A ready to use comparator, which will compare artifact information
     * objects by name
     */
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

    @Override
    public String toString ()
    {
        return String.format ( "[Artifact: %s - %s]", this.name, this.id );
    }

}
