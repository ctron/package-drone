package de.dentrassi.pm.storage.jpa;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue ( "G" )
public class GeneratedArtifactEntity extends DerivedArtifactEntity
{
}
