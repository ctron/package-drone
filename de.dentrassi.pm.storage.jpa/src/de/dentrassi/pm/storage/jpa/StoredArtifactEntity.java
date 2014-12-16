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

import static javax.persistence.CascadeType.REMOVE;

import java.util.Collection;
import java.util.LinkedList;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

@Entity
@DiscriminatorValue ( "S" )
public class StoredArtifactEntity extends ArtifactEntity
{
    @OneToMany ( orphanRemoval = true, mappedBy = "parent", cascade = REMOVE )
    private Collection<DerivedArtifactEntity> derivdedArtifacts = new LinkedList<> ();

    public void setDerivedArtifacts ( final Collection<DerivedArtifactEntity> derivdedArtifacts )
    {
        this.derivdedArtifacts = derivdedArtifacts;
    }

    public Collection<DerivedArtifactEntity> getDerivedArtifacts ()
    {
        return this.derivdedArtifacts;
    }
}
