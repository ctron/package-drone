/*******************************************************************************
 * Copyright (c) 2015 Jens Reimann.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jens Reimann - initial API and implementation
 *******************************************************************************/
package de.dentrassi.pm.maven;

public class MavenInformation
{
    private String groupId;

    private String artifactId;

    private String version;

    private String classifier;

    private String extension;

    private String snapshotVersion;

    private Integer buildNumber;

    public String getGroupId ()
    {
        return this.groupId;
    }

    public void setGroupId ( final String groupId )
    {
        this.groupId = groupId;
    }

    public String getArtifactId ()
    {
        return this.artifactId;
    }

    public void setArtifactId ( final String artifactId )
    {
        this.artifactId = artifactId;
    }

    public String getVersion ()
    {
        return this.version;
    }

    public void setVersion ( final String version )
    {
        this.version = version;
    }

    public String getClassifier ()
    {
        return this.classifier;
    }

    public void setClassifier ( final String classifier )
    {
        this.classifier = classifier;
    }

    public String getExtension ()
    {
        return this.extension;
    }

    public void setExtension ( final String extension )
    {
        this.extension = extension;
    }

    public String getSnapshotVersion ()
    {
        return this.snapshotVersion;
    }

    public void setSnapshotVersion ( final String snapshotVersion )
    {
        this.snapshotVersion = snapshotVersion;
    }

    public void setBuildNumber ( final Integer buildNumber )
    {
        this.buildNumber = buildNumber;
    }

    public Integer getBuildNumber ()
    {
        return this.buildNumber;
    }
}
