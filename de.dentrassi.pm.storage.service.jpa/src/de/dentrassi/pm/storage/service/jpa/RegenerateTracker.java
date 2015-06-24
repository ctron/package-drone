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

import de.dentrassi.osgi.profiler.Profile;
import de.dentrassi.osgi.profiler.Profile.Handle;
import de.dentrassi.pm.aspect.listener.PostAddContext;
import de.dentrassi.pm.storage.jpa.ChannelEntity;
import de.dentrassi.pm.storage.jpa.GeneratorArtifactEntity;

public class RegenerateTracker
{
    private final Set<GeneratorArtifactEntity> artifacts = new HashSet<> ();

    private final ChannelEntity channel;

    private boolean markedAdded;

    public RegenerateTracker ( final ChannelEntity channel )
    {
        this.channel = channel;
    }

    public void add ( final GeneratorArtifactEntity artifact )
    {
        this.artifacts.add ( artifact );
    }

    public void process ( final StorageHandlerImpl handler )
    {
        try ( Handle handle = Profile.start ( this, "process" ) )
        {
            for ( final GeneratorArtifactEntity artifact : this.artifacts )
            {
                regenerateArtifact ( artifact, handler );
            }

            this.artifacts.clear ();

            if ( this.markedAdded )
            {
                try ( Handle h2 = Profile.start ( "handle marked added" ) )
                {
                    final RegenerateTracker tracker = new RegenerateTracker ( this.channel );

                    final PostAddContext postAddContex = new PostAddContentImpl ( handler, this.channel.getId (), tracker );
                    handler.runChannelListeners ( this.channel, listener -> listener.artifactAdded ( postAddContex ) );

                    tracker.process ( handler );
                }
            }
        }
    }

    private void regenerateArtifact ( final GeneratorArtifactEntity artifact, final StorageHandlerImpl handler )
    {
        try
        {
            handler.regenerateArtifact ( artifact, false );
        }
        catch ( final Exception e )
        {
            throw new RuntimeException ( e );
        }
    }

    public void markAdded ()
    {
        this.markedAdded = true;
    }

}
