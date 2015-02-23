/*******************************************************************************
 * Copyright (c) 2014, 2015 Jens Reimann.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jens Reimann - initial API and implementation
 *******************************************************************************/
package de.dentrassi.pm.storage.service.jpa;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import de.dentrassi.pm.common.ChannelAspectInformation;
import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.common.SimpleArtifactInformation;
import de.dentrassi.pm.storage.Artifact;
import de.dentrassi.pm.storage.ArtifactReceiver;
import de.dentrassi.pm.storage.Channel;
import de.dentrassi.pm.storage.DeployGroup;
import de.dentrassi.pm.storage.DeployKey;

public class ChannelImpl implements Channel
{
    private final String id;

    private final boolean locked;

    private final StorageServiceImpl service;

    private final String name;

    public ChannelImpl ( final String id, final String name, final boolean locked, final StorageServiceImpl service )
    {
        this.id = id;
        this.name = name;
        this.locked = locked;
        this.service = service;
    }

    @Override
    public boolean isLocked ()
    {
        return this.locked;
    }

    @Override
    public boolean hasAspect ( final String id )
    {
        final Set<String> result = this.service.getChannelAspects ( this.id );
        return result.contains ( id );
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
    public Collection<DeployKey> getAllDeployKeys ()
    {
        return this.service.getAllDeployKeys ( this.id );
    }

    @Override
    public Collection<DeployGroup> getDeployGroups ()
    {
        return this.service.getDeployGroups ( this.id );
    }

    @Override
    public void addDeployGroup ( final String groupId )
    {
        this.service.addDeployGroup ( this.id, groupId );
    }

    @Override
    public void removeDeployGroup ( final String groupId )
    {
        this.service.removeDeployGroup ( this.id, groupId );
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

    @Override
    public Set<SimpleArtifactInformation> getSimpleArtifacts ()
    {
        return this.service.listSimpleArtifacts ( this.id );
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

    @Override
    public void applyMetaData ( final Map<MetaKey, String> metadata )
    {
        this.service.applyChannelMetaData ( this.id, metadata );
    }

    @Override
    public SortedMap<MetaKey, String> getMetaData ()
    {
        return this.service.getChannelMetaData ( this.id );
    }

    @Override
    public SortedMap<MetaKey, String> getProvidedMetaData ()
    {
        return this.service.getChannelProvidedMetaData ( this.id );
    }

}
