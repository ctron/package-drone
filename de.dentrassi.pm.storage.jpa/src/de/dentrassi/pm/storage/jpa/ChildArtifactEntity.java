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

import static javax.persistence.FetchType.LAZY;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public abstract class ChildArtifactEntity extends ArtifactEntity
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
