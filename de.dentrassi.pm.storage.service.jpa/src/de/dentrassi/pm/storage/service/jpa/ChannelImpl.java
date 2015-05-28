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
package de.dentrassi.pm.storage.service.jpa;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import de.dentrassi.pm.common.ArtifactInformation;
import de.dentrassi.pm.common.ChannelAspectInformation;
import de.dentrassi.pm.common.DetailedArtifactInformation;
import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.common.SimpleArtifactInformation;
import de.dentrassi.pm.common.utils.ThrowingConsumer;
import de.dentrassi.pm.storage.Artifact;
import de.dentrassi.pm.storage.ArtifactReceiver;
import de.dentrassi.pm.storage.CacheEntry;
import de.dentrassi.pm.storage.CacheEntryInformation;
import de.dentrassi.pm.storage.Channel;
import de.dentrassi.pm.storage.DeployGroup;
import de.dentrassi.pm.storage.DeployKey;
import de.dentrassi.pm.storage.ValidationMessage;

public class ChannelImpl implements Channel
{
    private final String id;

    private final boolean locked;

    private final StorageServiceImpl service;

    private final String name;

    private final String description;

    private final long warnings;

    private final long errors;

    public ChannelImpl ( final String id, final String name, final String description, final boolean locked, final long warnings, final long errors, final StorageServiceImpl service )
    {
        this.id = id;
        this.name = name;
        this.locked = locked;
        this.service = service;

        this.warnings = warnings;
        this.errors = errors;

        this.description = description;
    }

    @Override
    public String getDescription ()
    {
        return this.description;
    }

    @Override
    public boolean isLocked ()
    {
        return this.locked;
    }

    @Override
    public long getValidationWarningCount ()
    {
        return this.warnings;
    }

    @Override
    public long getValidationErrorCount ()
    {
        return this.errors;
    }

    @Override
    public List<ValidationMessage> getValidationMessages ()
    {
        return this.service.getValidationMessages ( this.id );
    }

    @Override
    public void streamCacheEntry ( final MetaKey key, final ThrowingConsumer<CacheEntry> consumer ) throws FileNotFoundException
    {
        this.service.streamCacheEntry ( this.id, key.getNamespace (), key.getKey (), consumer );
    }

    @Override
    public boolean hasAspect ( final String id )
    {
        return this.service.getChannelAspects ( this.id ).containsKey ( id );
    }

    @Override
    public void addAspects ( final boolean withDependencies, final String... aspectIds )
    {
        this.service.addChannelAspects ( this.id, new HashSet<> ( Arrays.asList ( aspectIds ) ), withDependencies );
    }

    @Override
    public void removeAspects ( final String... aspectIds )
    {
        this.service.removeChannelAspects ( this.id, new HashSet<> ( Arrays.asList ( aspectIds ) ) );
    }

    @Override
    public Artifact createArtifact ( final String name, final InputStream stream, final Map<MetaKey, String> providedMetaData )
    {
        return this.service.createArtifact ( this.id, name, stream, providedMetaData );
    }

    @Override
    public List<Artifact> findByName ( final String artifactName )
    {
        return this.service.findByName ( this.id, artifactName );
    }

    @Override
    public Artifact getArtifact ( final String artifactId )
    {
        final Artifact art = this.service.getArtifact ( artifactId );
        if ( art == null )
        {
            return null;
        }

        if ( !art.getChannel ().getId ().equals ( this.id ) )
        {
            return null;
        }

        return art;
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

    @Override
    public Set<ArtifactInformation> getArtifactInformations ()
    {
        return this.service.listArtifactInformations ( this.id );
    }

    @Override
    public Set<DetailedArtifactInformation> getDetailedArtifacts ()
    {
        return this.service.listDetailedArtifacts ( this.id );
    }

    public void streamData ( final String artifactId, final ArtifactReceiver consumer ) throws FileNotFoundException
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

    @Override
    public Set<String> getAspectIds ()
    {
        return this.service.getChannelAspects ( this.id ).keySet ();
    }

    @Override
    public Map<String, String> getAspectStates ()
    {
        return this.service.getChannelAspects ( this.id );
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

    @Override
    public void lock ()
    {
        this.service.lockChannel ( this.id );
    }

    @Override
    public void unlock ()
    {
        this.service.unlockChannel ( this.id );
    }

    @Override
    public List<CacheEntryInformation> getAllCacheEntries ()
    {
        return this.service.getAllCacheEntries ( this.id );
    }

    public List<ValidationMessage> getValidationMessagesForArtifact ( final String artifactId )
    {
        return this.service.getValidationMessagesForArtifact ( artifactId );
    }

    @Override
    public int hashCode ()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( this.id == null ? 0 : this.id.hashCode () );
        return result;
    }

    @Override
    public boolean equals ( final Object obj )
    {
        if ( this == obj )
        {
            return true;
        }
        if ( obj == null )
        {
            return false;
        }
        if ( ! ( obj instanceof Channel ) )
        {
            return false;
        }
        final Channel other = (Channel)obj;
        if ( getId () == null )
        {
            if ( other.getId () != null )
            {
                return false;
            }
        }
        else if ( !getId ().equals ( other.getId () ) )
        {
            return false;
        }
        return true;
    }

}
