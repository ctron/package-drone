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
package de.dentrassi.pm.storage;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import de.dentrassi.pm.common.ChannelAspectInformation;
import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.common.SimpleArtifactInformation;
import de.dentrassi.pm.common.utils.ThrowingConsumer;

/**
 * A handle to a channel
 * <p>
 * This class represents handle to a channel in a storage service. It can be
 * held for a longer period of time in order to operate on this channel. However
 * the channel might get deleted or modified in the meantime.
 * </p>
 */
public interface Channel
{
    public String getId ();

    /**
     * A unique alias to the channel id
     *
     * @return the alias name or <code>null</code> if none is set
     */
    public String getName ();

    public default String getNameOrId ()
    {
        final String name = getName ();
        if ( name != null && !name.isEmpty () )
        {
            return name;
        }
        return getId ();
    }

    /**
     * A plain text description of this channel
     *
     * @return the description
     */
    public String getDescription ();

    boolean isLocked ();

    /**
     * Lock the channel for further modifications <br/>
     * Note that the channel can still be renamed and data can be accessed.
     * However the channel cannot be deleted or any contained artifacts be
     * modified.
     * <p>
     * If the channel is already locked nothing will happen.
     * </p>
     */
    public void lock ();

    /**
     * Reverses a channel lock <br/>
     * Undoes the effects of {@link #lock()}
     * <p>
     * If the channel is already unlocked nothing will happen.
     * </p>
     */
    public void unlock ();

    public Set<Artifact> getArtifacts ();

    public List<ChannelAspectInformation> getAspects ();

    /**
     * Get a set of assigned channel aspect IDs
     * 
     * @return the set of assigned IDs, never returns <code>null</code>
     */
    public Set<String> getAspectIds ();

    public boolean hasAspect ( String id );

    public Artifact createArtifact ( String name, InputStream stream, Map<MetaKey, String> providedMetaData );

    public List<Artifact> findByName ( String artifactName );

    public Set<SimpleArtifactInformation> getSimpleArtifacts ();

    public SortedMap<MetaKey, String> getMetaData ();

    public SortedMap<MetaKey, String> getProvidedMetaData ();

    public void applyMetaData ( Map<MetaKey, String> metadata );

    public Collection<DeployKey> getAllDeployKeys ();

    public Collection<DeployGroup> getDeployGroups ();

    public void addDeployGroup ( String groupId );

    public void removeDeployGroup ( String groupId );

    /**
     * Stream the data of a cache entry to the consumer
     * <p>
     * The {@link CacheEntry#getStream()} may only be called while consuming the
     * cache entry. The stream will be closed automatically before this method
     * returns.
     * </p>
     *
     * @param key
     *            the key of the cache entry
     * @param consumer
     *            the consumer of the cache entry
     * @throws FileNotFoundException
     *             if the cache entry does not exists
     */
    public void streamCacheEntry ( MetaKey key, ThrowingConsumer<CacheEntry> consumer ) throws FileNotFoundException;

    /**
     * Get information about all present cache entries
     *
     * @return the list of information
     */
    public List<CacheEntryInformation> getAllCacheEntries ();

}
