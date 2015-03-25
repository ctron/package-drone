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
package de.dentrassi.pm.storage.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table ( name = "ARTIFACT_DELETE_QUEUE" )
public class ArtifactDeleteRequestEntity
{
    @Id
    @Column ( name = "ARTIFACT_ID", unique = true )
    private String artifactId;

    public void setArtifactId ( final String id )
    {
        this.artifactId = id;
    }

    public String getArtifactId ()
    {
        return this.artifactId;
    }

}
