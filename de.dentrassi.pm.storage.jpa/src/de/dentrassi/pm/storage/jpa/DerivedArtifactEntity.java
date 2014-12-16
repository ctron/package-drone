package de.dentrassi.pm.storage.jpa;

import static javax.persistence.FetchType.LAZY;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public abstract class DerivedArtifactEntity extends ArtifactEntity
{
    @ManyToOne ( fetch = LAZY )
    @JoinColumn ( name = "PARENT" )
    private ArtifactEntity parent;

    public void setParent ( final ArtifactEntity parent )
    {
        this.parent = parent;
    }

    public ArtifactEntity getParent ()
    {
        return this.parent;
    }

}
