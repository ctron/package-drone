/*******************************************************************************
 * Copyright (c) 2014, 2015 Jens Reimann.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jens Reimann - initial API and implementation
 *******************************************************************************/
package de.dentrassi.pm.storage.jpa;

import static javax.persistence.FetchType.LAZY;

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
public abstract class ArtifactPropertyEntity implements PropertyEntity
{
    @Id
    @ManyToOne ( fetch = LAZY )
    @JoinColumn ( name = "ART_ID", referencedColumnName = "ID" )
    private ArtifactEntity artifact;

    @Id
    @Column ( name = "NS" )
    private String namespace;

    @Id
    @Column ( name = "\"KEY\"" )
    private String key;

    @Column ( name = "\"VALUE\"" )
    private String value;

    public void setArtifact ( final ArtifactEntity artifact )
    {
        this.artifact = artifact;
    }

    public ArtifactEntity getArtifact ()
    {
        return this.artifact;
    }

    @Override
    public void setKey ( final String key )
    {
        this.key = key;
    }

    @Override
    public String getKey ()
    {
        return this.key;
    }

    @Override
    public void setNamespace ( final String namespace )
    {
        this.namespace = namespace;
    }

    @Override
    public String getNamespace ()
    {
        return this.namespace;
    }

    @Override
    public void setValue ( final String value )
    {
        this.value = value;
    }

    @Override
    public String getValue ()
    {
        return this.value;
    }

}
