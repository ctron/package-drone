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

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.collect.Multimap;

import de.dentrassi.pm.common.ArtifactInformation;
import de.dentrassi.pm.common.DetailedArtifactInformation;
import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.common.utils.ThrowingConsumer;
import de.dentrassi.pm.storage.CacheEntry;
import de.dentrassi.pm.storage.CacheEntryInformation;
import de.dentrassi.pm.storage.jpa.ArtifactEntity;
import de.dentrassi.pm.storage.jpa.ChannelEntity;

public interface StorageHandler
{
    public void addChannelAspects ( final String channelId, final Set<String> aspects, final boolean withDependencies );

    public void clearChannel ( final String channelId );

    public ArtifactEntity createAttachedArtifact ( final String parentArtifactId, final String name, final InputStream stream, final Map<MetaKey, String> providedMetaData );

    public ChannelEntity createChannel ( final String name, final String description, final Map<MetaKey, String> providedMetaData );

    public ArtifactInformation deleteArtifact ( String artifactId );

    public void deleteChannel ( String channelId, boolean ignoreLock );

    public void generateArtifact ( String id );

    public List<CacheEntryInformation> getAllCacheEntries ( String channelId );

    public Set<ArtifactInformation> getArtifacts ( final String channelId );

    public Multimap<String, MetaDataEntry> getChannelArtifactProperties ( ChannelEntity channel );

    public SortedMap<MetaKey, String> getChannelMetaData ( String channelId );

    public SortedMap<MetaKey, String> getChannelProvidedMetaData ( String channelId );

    public ChannelEntity getCheckedChannel ( final String channelId );

    public Set<DetailedArtifactInformation> getDetailedArtifacts ( ChannelEntity channel );

    public ArtifactEntity internalCreateArtifact ( String channelId, String name, Supplier<ArtifactEntity> entityCreator, InputStream stream, Map<MetaKey, String> providedMetaData, boolean external );

    public <T extends Comparable<? super T>> Set<T> listArtifacts ( final ChannelEntity ce, final Function<ArtifactEntity, T> mapper );

    public void reprocessAspects ( ChannelEntity channel, Set<String> aspectFactoryIds ) throws Exception;

    public boolean streamCacheEntry ( final String channelId, final String namespace, final String key, final ThrowingConsumer<CacheEntry> consumer );

    public void updateChannel ( final String channelId, final String name, final String description );

    public void wipeAllChannels ();
}
