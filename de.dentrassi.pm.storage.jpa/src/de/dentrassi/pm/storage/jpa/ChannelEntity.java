package de.dentrassi.pm.storage.jpa;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import org.eclipse.persistence.annotations.UuidGenerator;

@Entity ( name = "CHANNELS" )
@UuidGenerator ( name = "CHAN_UUID_GEN" )
public class ChannelEntity
{
    @Id
    @GeneratedValue ( generator = "CHAN_UUID_GEN" )
    private String id;

    @OneToMany ( mappedBy = "channel" )
    private Set<ArtifactEntity> artifacts = new HashSet<> ();

    public void setId ( final String id )
    {
        this.id = id;
    }

    public String getId ()
    {
        return this.id;
    }

    public Set<ArtifactEntity> getArtifacts ()
    {
        return this.artifacts;
    }

    public void setArtifacts ( final Set<ArtifactEntity> artifacts )
    {
        this.artifacts = artifacts;
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
