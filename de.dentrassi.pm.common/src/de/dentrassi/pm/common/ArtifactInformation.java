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
import java.util.Date;
import java.util.SortedMap;

public class ArtifactInformation extends SimpleArtifactInformation
{
    public ArtifactInformation ( final String id, final String parentId, final long size, final String name, final String channelId, final Date creationTimestamp, final boolean derived, final SortedMap<MetaKey, String> metaData )
    {
        super ( id, parentId, size, name, channelId, creationTimestamp, derived );
        this.metaData = Collections.unmodifiableSortedMap ( metaData );
    }

    private final SortedMap<MetaKey, String> metaData;

    public SortedMap<MetaKey, String> getMetaData ()
    {
        return this.metaData;
    }
}
