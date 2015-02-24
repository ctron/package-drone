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
package de.dentrassi.pm.maven;

import de.dentrassi.pm.common.MetaKeyBinding;

public class MavenInformation
{
    @MetaKeyBinding ( namespace = "mvn", key = "groupId" )
    private String groupId;

    @MetaKeyBinding ( namespace = "mvn", key = "artifactId" )
    private String artifactId;

    @MetaKeyBinding ( namespace = "mvn", key = "version" )
    private String version;

    @MetaKeyBinding ( namespace = "mvn", key = "classifier" )
    private String classifier;

    @MetaKeyBinding ( namespace = "mvn", key = "extension" )
    private String extension;

    @MetaKeyBinding ( namespace = "mvn", key = "snapshotVersion" )
    private String snapshotVersion;

    @MetaKeyBinding ( namespace = "mvn", key = "buildNumber" )
    private Long buildNumber;

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

    public void setBuildNumber ( final Long buildNumber )
    {
        this.buildNumber = buildNumber;
    }

    public Long getBuildNumber ()
    {
        return this.buildNumber;
    }

    public String makePath ()
    {
        final StringBuilder sb = new StringBuilder ();

        appendPath ( sb );
        sb.append ( '/' );
        appendFile ( sb, false );

        return sb.toString ();
    }

    public String makeName ()
    {
        final StringBuilder sb = new StringBuilder ();
        appendFile ( sb, false );
        return sb.toString ();
    }

    public String makePlainName ()
    {
        final StringBuilder sb = new StringBuilder ();
        appendFile ( sb, true );
        return sb.toString ();
    }

    protected void appendFile ( final StringBuilder sb, final boolean ignoreClassifier )
    {
        sb.append ( this.artifactId );
        sb.append ( '-' );

        if ( this.snapshotVersion != null )
        {
            sb.append ( this.snapshotVersion );
        }
        else
        {
            sb.append ( this.version );
        }

        if ( this.classifier != null && !ignoreClassifier )
        {
            sb.append ( '-' );
            sb.append ( this.classifier );
        }
        sb.append ( '.' );
        sb.append ( this.extension );
    }

    protected void appendPath ( final StringBuilder sb )
    {
        sb.append ( this.groupId );
        sb.append ( '/' );
        sb.append ( this.artifactId );
        sb.append ( '/' );
        sb.append ( this.version );
    }

    public boolean isSnapshot ()
    {
        return this.version != null && this.version.endsWith ( "-SNAPSHOT" );
    }
}
