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

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.FetchType.EAGER;

import java.util.Collection;
import java.util.LinkedList;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.CascadeOnDelete;
import org.eclipse.persistence.annotations.UuidGenerator;

@Entity
@Table ( name = "DEPLOY_GROUPS" )
@UuidGenerator ( name = "DG_UUID_GEN" )
public class DeployGroupEntity
{
    @Id
    @Column ( nullable = false, length = 36 )
    @GeneratedValue ( generator = "DG_UUID_GEN" )
    private String id;

    @Column ( length = 255 )
    private String name;

    @OneToMany ( fetch = EAGER, orphanRemoval = true, cascade = ALL, mappedBy = "group" )
    @CascadeOnDelete
    private Collection<DeployKeyEntity> keys = new LinkedList<> ();

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

    public void setKeys ( final Collection<DeployKeyEntity> keys )
    {
        this.keys = keys;
    }

    public Collection<DeployKeyEntity> getKeys ()
    {
        return this.keys;
    }

    @Override
    public int hashCode ()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( this.id == null ? 0 : this.id.hashCode () );
        result = prime * result + ( this.name == null ? 0 : this.name.hashCode () );
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
        if ( ! ( obj instanceof DeployGroupEntity ) )
        {
            return false;
        }
        final DeployGroupEntity other = (DeployGroupEntity)obj;
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
        if ( this.name == null )
        {
            if ( other.name != null )
            {
                return false;
            }
        }
        else if ( !this.name.equals ( other.name ) )
        {
            return false;
        }
        return true;
    }

}
