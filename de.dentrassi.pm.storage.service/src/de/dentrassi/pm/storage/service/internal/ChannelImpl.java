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
package de.dentrassi.pm.storage.service.internal;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.dentrassi.pm.aspect.ChannelAspectInformation;
import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.storage.Artifact;
import de.dentrassi.pm.storage.ArtifactReceiver;
import de.dentrassi.pm.storage.Channel;

public class ChannelImpl implements Channel
{
    private final String id;

    private final StorageServiceImpl service;

    private final String name;

    public ChannelImpl ( final String id, final String name, final StorageServiceImpl service )
    {
        this.id = id;
        this.name = name;
        this.service = service;
    }

    @Override
    public Artifact createArtifact ( final String name, final InputStream stream, final Map<MetaKey, String> providedMetaData )
    {
        return this.service.createArtifact ( this.id, name, stream, providedMetaData );
    }

    @Override
    public Collection<Artifact> findByName ( final String artifactName )
    {
        return this.service.findByName ( this.id, artifactName );
    }

    @Override
    public String getName ()
    {
        return this.name;
    }

    @Override
    public String getId ()
    {
        return this.id;
    }

    @Override
    public Set<Artifact> getArtifacts ()
    {
        return this.service.listArtifacts ( this.id );
    }

    public void streamData ( final String artifactId, final ArtifactReceiver consumer )
    {
        this.service.streamArtifact ( artifactId, consumer );
    }

    StorageServiceImpl getService ()
    {
        return this.service;
    }

    @Override
    public List<ChannelAspectInformation> getAspects ()
    {
        return this.service.getChannelAspectInformations ( this.id );
    }

    public void generate ( final String id )
    {
        this.service.generateArtifact ( id );
    }
}
