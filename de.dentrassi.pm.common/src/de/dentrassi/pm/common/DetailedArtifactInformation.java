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
package de.dentrassi.pm.common;

import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.SortedMap;

public class DetailedArtifactInformation extends SimpleArtifactInformation
{
    private final SortedMap<MetaKey, String> metaData;

    public DetailedArtifactInformation ( final String id, final String parentId, final long size, final String name, final String channelId, final Date creationTimestamp, final long warnings, final long errors, final Set<String> facets, final SortedMap<MetaKey, String> metaData )
    {
        super ( id, parentId, size, name, channelId, creationTimestamp, warnings, errors, facets );
        this.metaData = Collections.unmodifiableSortedMap ( metaData );
    }

    /**
     * Get the combined artifact meta data
     *
     * @return an unmodifiable set of meta data
     */
    public SortedMap<MetaKey, String> getMetaData ()
    {
        return this.metaData;
    }

}
