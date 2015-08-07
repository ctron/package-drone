package de.dentrassi.pm.storage.channel;

import java.time.Instant;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import de.dentrassi.pm.common.Validated;

public class ArtifactInformation implements Comparable<ArtifactInformation>, Validated
{
    private final String id;

    private final String name;

    private final long size;

    private final Instant creationTimestamp;

    private final Set<String> types;

    public ArtifactInformation ( final String id, final String name, final long size, final Instant creationTimestamp, final Set<String> types )
    {
        this.id = id;
        this.name = name;
        this.size = size;
        this.creationTimestamp = creationTimestamp;
        this.types = new CopyOnWriteArraySet<> ( types );
    }

    public boolean is ( final String type )
    {
        return this.types.contains ( type );
    }

    public String getId ()
    {
        return this.id;
    }

    public String getName ()
    {
        return this.name;
    }

    public long getSize ()
    {
        return this.size;
    }

    public Instant getCreationInstant ()
    {
        return this.creationTimestamp;
    }

    public Date getCreationTimestamp ()
    {
        return new Date ( this.creationTimestamp.toEpochMilli () );
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
        if ( ! ( obj instanceof ArtifactInformation ) )
        {
            return false;
        }
        final ArtifactInformation other = (ArtifactInformation)obj;
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

    @Override
    public int compareTo ( final ArtifactInformation o )
    {
        return this.id.compareTo ( o.id );
    }

    @Override
    public long getValidationErrorCount ()
    {
        // FIXME: implement
        return 0;
    }

    @Override
    public long getValidationWarningCount ()
    {
        // FIXME: implement
        return 0;
    }

}
