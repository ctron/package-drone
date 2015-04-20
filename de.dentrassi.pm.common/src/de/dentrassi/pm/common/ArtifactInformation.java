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

import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

/**
 * Full artifact information
 * <p>
 * The reason for the SimpleArtifactInformation and ArtifactInformation is that
 * reading the metadata actually is a costly operation, and sometimes you just
 * don't need the meta data information.
 * </p>
 */
public class ArtifactInformation extends DetailedArtifactInformation
{
    private final SortedSet<String> childIds;

    public ArtifactInformation ( final String id, final String parentId, final long size, final String name, final String channelId, final Date creationTimestamp, final Set<String> facets, final SortedMap<MetaKey, String> metaData, final SortedSet<String> childIds )
    {
        super ( id, parentId, size, name, channelId, creationTimestamp, facets, metaData );
        this.childIds = Collections.unmodifiableSortedSet ( childIds );
    }

    /**
     * Get the ID of all direct children
     * <p>
     * If the artifact does not have any children, and empty set is returned.
     * </p>
     *
     * @return an unmodifiable set of all direct child IDs, never returns
     *         <code>null</code>
     */
    public Set<String> getChildIds ()
    {
        return this.childIds;
    }

}
