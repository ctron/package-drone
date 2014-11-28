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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;

import org.eclipse.persistence.annotations.UuidGenerator;

@Entity ( name = "CHANNELS" )
@UuidGenerator ( name = "CHAN_UUID_GEN" )
public class ChannelEntity
{
    @Id
    @GeneratedValue ( generator = "CHAN_UUID_GEN" )
    private String id;

    @Column ( name = "NAME", nullable = true, unique = true )
    private String name;

    @OneToMany ( mappedBy = "channel" )
    private Set<ArtifactEntity> artifacts = new HashSet<> ();

    @ElementCollection
    @CollectionTable ( name = "CHANNEL_ASPECTS", joinColumns = @JoinColumn ( name = "CHANNEL_ID", nullable = false ) )
    @Column ( nullable = false, unique = true, name = "ASPECT" )
    private List<String> aspects = new ArrayList<> ();

    public void setId ( final String id )
    {
        this.id = id;
    }

    public String getId ()
    {
        return this.id;
    }

    public String getName ()
    {
        return this.name;
    }

    public void setName ( final String name )
    {
        this.name = name;
    }

    public Set<ArtifactEntity> getArtifacts ()
    {
        return this.artifacts;
    }

    public void setArtifacts ( final Set<ArtifactEntity> artifacts )
    {
        this.artifacts = artifacts;
    }

    public List<String> getAspects ()
    {
        return this.aspects;
    }

    public void setAspects ( final List<String> aspects )
    {
        this.aspects = aspects;
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
        if ( ! ( obj instanceof ChannelEntity ) )
        {
            return false;
        }
        final ChannelEntity other = (ChannelEntity)obj;
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
