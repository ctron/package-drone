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
package de.dentrassi.pm.common.event;

import java.util.Collections;
import java.util.SortedMap;

import de.dentrassi.pm.common.MetaKey;

public abstract class ArtifactEvent
{
    private final SortedMap<MetaKey, String> metaData;

    private final String artifactId;

    public ArtifactEvent ( final String artifactId, final SortedMap<MetaKey, String> metaData )
    {
        this.artifactId = artifactId;
        this.metaData = Collections.unmodifiableSortedMap ( metaData );
    }

    public String getArtifactId ()
    {
        return this.artifactId;
    }

    public SortedMap<MetaKey, String> getMetaData ()
    {
        return this.metaData;
    }

    @Override
    public String toString ()
    {
        return String.format ( "[%s - id: %s]", getClass ().getSimpleName (), this.artifactId );
    }
}
