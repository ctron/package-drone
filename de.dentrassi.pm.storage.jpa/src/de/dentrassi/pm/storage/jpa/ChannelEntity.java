/*******************************************************************************
 * Copyright (c) 2014, 2015 IBH SYSTEMS GmbH.
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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.MapKeyColumn;
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

    @Column ( name = "DESCRIPTION", nullable = true, length = 255 )
    private String description;

    @OneToMany ( mappedBy = "channel", orphanRemoval = true )
    private Set<ArtifactEntity> artifacts = new HashSet<> ();

    @ElementCollection
    @CollectionTable ( name = "CHANNEL_ASPECTS", joinColumns = @JoinColumn ( name = "CHANNEL_ID", nullable = false ) )
    @MapKeyColumn ( name = "ASPECT" )
    @Column ( name = "VERSION", nullable = true )
    private Map<String, String> aspects = new HashMap<> ();

    @OneToMany ( orphanRemoval = true, cascade = ALL, mappedBy = "channel" )
    private Collection<ExtractedChannelPropertyEntity> extractedProperties = new LinkedList<> ();

    @OneToMany ( orphanRemoval = true, cascade = ALL, mappedBy = "channel" )
    private Collection<ProvidedChannelPropertyEntity> providedProperties = new LinkedList<> ();

    @OneToMany
    @JoinTable ( name = "CHANNEL_DEPLOY_GROUPS",
            joinColumns = @JoinColumn ( name = "CHANNEL_ID", referencedColumnName = "id" ) ,
            inverseJoinColumns = @JoinColumn ( name = "GROUP_ID", referencedColumnName = "id" ) )
    private Set<DeployGroupEntity> deployGroups = new HashSet<> ();

    private boolean locked;

    @Column ( name = "AGR_NUM_WARN", nullable = false )
    private long aggregatedNumberOfWarnings;

    @Column ( name = "AGR_NUM_ERR", nullable = false )
    private long aggregatedNumberOfErrors;

    public long getAggregatedNumberOfErrors ()
    {
        return this.aggregatedNumberOfErrors;
    }

    public void setAggregatedNumberOfErrors ( final long aggregatedNumberOfErrors )
    {
        this.aggregatedNumberOfErrors = aggregatedNumberOfErrors;
    }

    public long getAggregatedNumberOfWarnings ()
    {
        return this.aggregatedNumberOfWarnings;
    }

    public void setAggregatedNumberOfWarnings ( final long aggregatedNumberOfWarnings )
    {
        this.aggregatedNumberOfWarnings = aggregatedNumberOfWarnings;
    }

    public void setLocked ( final boolean locked )
    {
        this.locked = locked;
    }

    public boolean isLocked ()
    {
        return this.locked;
    }

    public void setDescription ( final String description )
    {
        this.description = description;
    }

    public String getDescription ()
    {
        return this.description;
    }

    public void setDeployGroups ( final Set<DeployGroupEntity> deployGroups )
    {
        this.deployGroups = deployGroups;
    }

    public Set<DeployGroupEntity> getDeployGroups ()
    {
        return this.deployGroups;
    }

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

    public Map<String, String> getAspects ()
    {
        return this.aspects;
    }

    public void setAspects ( final Map<String, String> aspects )
    {
        this.aspects = aspects;
    }

    public Collection<ExtractedChannelPropertyEntity> getExtractedProperties ()
    {
        return this.extractedProperties;
    }

    public void setExtractedProperties ( final Collection<ExtractedChannelPropertyEntity> extractedProperties )
    {
        this.extractedProperties = extractedProperties;
    }

    public Collection<ProvidedChannelPropertyEntity> getProvidedProperties ()
    {
        return this.providedProperties;
    }

    public void setProvidedProperties ( final Collection<ProvidedChannelPropertyEntity> providedProperties )
    {
        this.providedProperties = providedProperties;
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
