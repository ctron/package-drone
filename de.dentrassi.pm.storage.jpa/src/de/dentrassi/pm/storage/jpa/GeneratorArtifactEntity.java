/*******************************************************************************
 * Copyright (c) 2014 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package de.dentrassi.pm.storage.jpa;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.SecondaryTable;

@Entity
@DiscriminatorValue ( "GEN" )
@SecondaryTable ( name = "GENERATED_ARTIFACTS" )
public class GeneratorArtifactEntity extends RootArtifactEntity
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
