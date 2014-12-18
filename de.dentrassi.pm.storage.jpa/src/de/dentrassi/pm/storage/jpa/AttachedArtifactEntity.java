package de.dentrassi.pm.storage.jpa;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue ( "A" )
public class AttachedArtifactEntity extends ChildArtifactEntity
{
}
