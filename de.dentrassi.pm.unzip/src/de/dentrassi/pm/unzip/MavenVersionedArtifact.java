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
package de.dentrassi.pm.unzip;

import org.apache.maven.artifact.versioning.ComparableVersion;

import de.dentrassi.pm.storage.Artifact;

public class MavenVersionedArtifact implements Comparable<MavenVersionedArtifact>
{
    private final ComparableVersion version;

    private final Artifact artifact;

    public MavenVersionedArtifact ( final ComparableVersion version, final Artifact artifact )
    {
        this.version = version;
        this.artifact = artifact;
    }

    public Artifact getArtifact ()
    {
        return this.artifact;
    }

    public ComparableVersion getVersion ()
    {
        return this.version;
    }

    @Override
    public int compareTo ( final MavenVersionedArtifact o )
    {
        return this.version.compareTo ( o.version );
    }
}
