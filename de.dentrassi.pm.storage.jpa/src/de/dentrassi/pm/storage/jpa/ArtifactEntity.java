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

import static javax.persistence.CascadeType.ALL;

import java.util.Collection;
import java.util.LinkedList;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.eclipse.persistence.annotations.UuidGenerator;

@Entity ( name = "ARTIFACTS" )
@Inheritance ( strategy = InheritanceType.SINGLE_TABLE )
@DiscriminatorColumn ( name = "TYPE" )
@UuidGenerator ( name = "CHAN_UUID_GEN" )
public abstract class ArtifactEntity
{
    @Id
    @Column ( name = "ID" )
    @GeneratedValue ( generator = "CHAN_UUID_GEN" )
    private String id;

    @ManyToOne
    @JoinColumn ( name = "CHANNEL_ID" )
    private ChannelEntity channel;

    @Basic
    private String name;

    @Basic
    private long size;

    @OneToMany ( orphanRemoval = true, cascade = ALL, mappedBy = "artifact" )
    private Collection<ExtractedArtifactPropertyEntity> extractedProperties = new LinkedList<> ();

    @OneToMany ( orphanRemoval = true, cascade = ALL, mappedBy = "artifact" )
    private Collection<ProvidedArtifactPropertyEntity> providedProperties = new LinkedList<> ();

    public String getId ()
    {
        return this.id;
    }

    public void setId ( final String id )
    {
        this.id = id;
    }

    public ChannelEntity getChannel ()
    {
        return this.channel;
    }

    public void setChannel ( final ChannelEntity channel )
    {
        this.channel = channel;
    }

    public void setName ( final String name )
    {
        this.name = name;
    }

    public String getName ()
    {
        return this.name;
    }

    public Collection<ExtractedArtifactPropertyEntity> getExtractedProperties ()
    {
        return this.extractedProperties;
    }

    public void setExtractedProperties ( final Collection<ExtractedArtifactPropertyEntity> extractedProperties )
    {
        this.extractedProperties = extractedProperties;
    }

    public Collection<ProvidedArtifactPropertyEntity> getProvidedProperties ()
    {
        return this.providedProperties;
    }

    public void setProvidedProperties ( final Collection<ProvidedArtifactPropertyEntity> providedProperties )
    {
        this.providedProperties = providedProperties;
    }

    public long getSize ()
    {
        return this.size;
    }

    public void setSize ( final long size )
    {
        this.size = size;
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
        if ( ! ( obj instanceof ArtifactEntity ) )
        {
            return false;
        }
        final ArtifactEntity other = (ArtifactEntity)obj;
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
