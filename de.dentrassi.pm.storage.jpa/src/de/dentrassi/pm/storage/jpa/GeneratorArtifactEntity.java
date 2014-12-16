package de.dentrassi.pm.storage.jpa;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.SecondaryTable;

@Entity
@DiscriminatorValue ( "GEN" )
@SecondaryTable ( name = "GENERATED_ARTIFACTS" )
public class GeneratorArtifactEntity extends StoredArtifactEntity
{
    @Column ( name = "GENERATOR_ID", nullable = false, table = "GENERATED_ARTIFACTS" )
    private String generatorId;

    public void setGeneratorId ( final String generatorId )
    {
        this.generatorId = generatorId;
    }

    public String getGeneratorId ()
    {
        return this.generatorId;
    }
}
