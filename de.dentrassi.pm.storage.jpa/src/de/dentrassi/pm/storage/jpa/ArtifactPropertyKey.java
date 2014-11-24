package de.dentrassi.pm.storage.jpa;

import java.io.Serializable;

public class ArtifactPropertyKey implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String artifactId;

    private String namespace;

    private String key;

    public void setArtifactId ( final String artifactId )
    {
        this.artifactId = artifactId;
    }

    public String getArtifactId ()
    {
        return this.artifactId;
    }

    public void setNamespace ( final String namespace )
    {
        this.namespace = namespace;
    }

    public void setKey ( final String key )
    {
        this.key = key;
    }

    public String getNamespace ()
    {
        return this.namespace;
    }

    public String getKey ()
    {
        return this.key;
    }

    @Override
    public int hashCode ()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( this.artifactId == null ? 0 : this.artifactId.hashCode () );
        result = prime * result + ( this.key == null ? 0 : this.key.hashCode () );
        result = prime * result + ( this.namespace == null ? 0 : this.namespace.hashCode () );
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
        if ( ! ( obj instanceof ArtifactPropertyKey ) )
        {
            return false;
        }
        final ArtifactPropertyKey other = (ArtifactPropertyKey)obj;
        if ( this.artifactId == null )
        {
            if ( other.artifactId != null )
            {
                return false;
            }
        }
        else if ( !this.artifactId.equals ( other.artifactId ) )
        {
            return false;
        }
        if ( this.key == null )
        {
            if ( other.key != null )
            {
                return false;
            }
        }
        else if ( !this.key.equals ( other.key ) )
        {
            return false;
        }
        if ( this.namespace == null )
        {
            if ( other.namespace != null )
            {
                return false;
            }
        }
        else if ( !this.namespace.equals ( other.namespace ) )
        {
            return false;
        }
        return true;
    }

}
