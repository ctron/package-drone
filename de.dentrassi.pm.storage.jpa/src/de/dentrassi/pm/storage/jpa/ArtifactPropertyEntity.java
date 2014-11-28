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
package de.dentrassi.pm.storage.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
@IdClass ( ArtifactPropertyKey.class )
@Inheritance ( strategy = InheritanceType.TABLE_PER_CLASS )
public abstract class ArtifactPropertyEntity
{
    @Id
    @Column ( name = "ART_ID", insertable = false, updatable = false )
    private String artifactId;

    @Id
    @Column ( name = "NS" )
    private String namespace;

    @Id
    @Column ( name = "\"KEY\"" )
    private String key;

    @Column ( name = "\"VALUE\"" )
    private String value;

    @ManyToOne
    @JoinColumn ( name = "ART_ID", referencedColumnName = "ID" )
    private ArtifactEntity artifact;

    public void setArtifact ( final ArtifactEntity artifact )
    {
        this.artifact = artifact;
    }

    public ArtifactEntity getArtifact ()
    {
        return this.artifact;
    }

    public void setKey ( final String key )
    {
        this.key = key;
    }

    public String getKey ()
    {
        return this.key;
    }

    public void setNamespace ( final String namespace )
    {
        this.namespace = namespace;
    }

    public String getNamespace ()
    {
        return this.namespace;
    }

    public void setValue ( final String value )
    {
        this.value = value;
    }

    public String getValue ()
    {
        return this.value;
    }

}
