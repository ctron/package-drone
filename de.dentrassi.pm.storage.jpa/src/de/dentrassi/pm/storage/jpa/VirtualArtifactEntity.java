/*******************************************************************************
 * Copyright (c) 2014 Jens Reimann.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jens Reimann - initial API and implementation
 *******************************************************************************/
package de.dentrassi.pm.storage.jpa;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.SecondaryTable;

@Entity
@DiscriminatorValue ( "V" )
@SecondaryTable ( name = "VIRTUAL_ARTIFACTS" )
public class VirtualArtifactEntity extends DerivedArtifactEntity
{
    @Column ( name = "NS", table = "VIRTUAL_ARTIFACTS", nullable = false )
    private String namespace;

    public void setNamespace ( final String namespace )
    {
        this.namespace = namespace;
    }

    public String getNamespace ()
    {
        return this.namespace;
    }
}
