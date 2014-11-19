package de.dentrassi.pm.storage.jpa;

import java.sql.Blob;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;

import org.eclipse.persistence.annotations.UuidGenerator;

@Entity ( name = "ARTIFACTS" )
@UuidGenerator ( name = "CHAN_UUID_GEN" )
public class ArtifactEntity
{
    @Id
    @Column ( name = "ID" )
    @GeneratedValue ( generator = "CHAN_UUID_GEN" )
    private String id;

    @ManyToOne
    @JoinColumn ( name = "CHANNEL" )
    private ChannelEntity channel;

    @Lob
    @Basic ( fetch = FetchType.LAZY )
    private Blob data;

    @ElementCollection
    @JoinTable ( name = "ARTIFACT_PROPERTIES", joinColumns = @JoinColumn ( name = "ART_ID" ) )
    @MapKeyColumn ( name = "KEY" )
    @Column ( name = "VALUE" )
    private final Map<String, String> properties = new HashMap<> ();

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

    public Blob getData ()
    {
        return this.data;
    }

    public void setData ( final Blob data )
    {
        this.data = data;
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
