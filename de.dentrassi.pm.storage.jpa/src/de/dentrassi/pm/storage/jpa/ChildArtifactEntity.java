/*******************************************************************************
 * Copyright (c) 2014, 2015 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package de.dentrassi.pm.storage.jpa;

import static javax.persistence.FetchType.LAZY;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PostPersist;

@Entity
public abstract class ChildArtifactEntity extends ArtifactEntity
{
    @ManyToOne ( fetch = LAZY )
    @JoinColumn ( name = "PARENT" )
    private ArtifactEntity parent;

    @Column ( name = "PARENT", updatable = false, insertable = false )
    private String parentId;

    public void setParent ( final ArtifactEntity parent )
    {
        internalSetParentId ( parent );
        this.parent = parent;
    }

    public ArtifactEntity getParent ()
    {
        return this.parent;
    }

    public String getParentId ()
    {
        return this.parentId;
    }

    @PostPersist
    public void updateId ()
    {
        // update parent id
        internalSetParentId ( this.parent );
    }

    private void internalSetParentId ( final ArtifactEntity parent )
    {
        if ( parent == null )
        {
            this.parentId = null;
        }
        else
        {
            this.parentId = parent.getId ();
        }
    }
}
