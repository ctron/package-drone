/*******************************************************************************
 * Copyright (c) 2015 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package de.dentrassi.pm.storage.service.jpa;

import java.util.HashSet;
import java.util.Set;

import de.dentrassi.pm.storage.jpa.GeneratorArtifactEntity;

public class RegenerateTracker
{
    private final Set<GeneratorArtifactEntity> artifacts = new HashSet<> ();

    private final StorageHandlerImpl handler;

    public RegenerateTracker ( final StorageHandlerImpl handler )
    {
        this.handler = handler;
    }

    public void add ( final GeneratorArtifactEntity artifact )
    {
        this.artifacts.add ( artifact );
    }

    public void process ( final boolean runAggregator )
    {
        if ( this.artifacts.isEmpty () )
        {
            return;
        }

        for ( final GeneratorArtifactEntity artifact : this.artifacts )
        {
            regenerateArtifact ( artifact, runAggregator );
        }

        this.artifacts.clear ();
    }

    private void regenerateArtifact ( final GeneratorArtifactEntity artifact, final boolean runAggregator )
    {
        try
        {
            this.handler.regenerateArtifact ( artifact, runAggregator );
        }
        catch ( final Exception e )
        {
            throw new RuntimeException ( e );
        }
    }

}
