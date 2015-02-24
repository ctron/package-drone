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

import static javax.persistence.FetchType.LAZY;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.eclipse.persistence.annotations.UuidGenerator;

@Entity
@Table ( name = "DEPLOY_KEYS" )
@UuidGenerator ( name = "DK_UUID_GEN" )
public class DeployKeyEntity
{
    @Id
    @Column ( nullable = false, length = 36 )
    @GeneratedValue ( generator = "DK_UUID_GEN" )
    private String id;

    @Column ( name = "NAME", length = 255 )
    private String name;

    @Column ( name = "KEY_DATA", nullable = false, length = 1024 )
    private String keyData;

    @Temporal ( value = TemporalType.TIMESTAMP )
    @Column ( name = "CREATION_TS", nullable = false, updatable = false )
    private Date creationTimestamp;

    @OneToOne ( fetch = LAZY, optional = true, orphanRemoval = false )
    private DeployGroupEntity group;

    public void setId ( final String id )
    {
        this.id = id;
    }

    public String getId ()
    {
        return this.id;
    }

    public void setName ( final String name )
    {
        this.name = name;
    }

    public String getName ()
    {
        return this.name;
    }

    public void setKeyData ( final String keyData )
    {
        this.keyData = keyData;
    }

    public String getKeyData ()
    {
        return this.keyData;
    }

    public void setCreationTimestamp ( final Date creationTimestamp )
    {
        this.creationTimestamp = creationTimestamp;
    }

    public Date getCreationTimestamp ()
    {
        return this.creationTimestamp;
    }

    public void setGroup ( final DeployGroupEntity group )
    {
        this.group = group;
    }

    public DeployGroupEntity getGroup ()
    {
        return this.group;
    }

    @Override
    public int hashCode ()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( this.id == null ? 0 : this.id.hashCode () );
        return result;
    }

    @Override
    public boolean equals ( final Object obj )
    {
        if ( this == obj )
        {
            return true;
        }
        if ( obj == null )
        {
            return false;
        }
        if ( ! ( obj instanceof DeployKeyEntity ) )
        {
            return false;
        }
        final DeployKeyEntity other = (DeployKeyEntity)obj;
        if ( this.id == null )
        {
            if ( other.id != null )
            {
                return false;
            }
        }
        else if ( !this.id.equals ( other.id ) )
        {
            return false;
        }
        return true;
    }

}
