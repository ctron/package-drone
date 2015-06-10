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
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import org.eclipse.scada.utils.lang.Holder;

import com.google.common.io.ByteStreams;

import de.dentrassi.pm.common.ArtifactInformation;
import de.dentrassi.pm.common.ChannelAspectInformation;
import de.dentrassi.pm.common.DetailedArtifactInformation;
import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.common.SimpleArtifactInformation;
import de.dentrassi.pm.common.Validated;
import de.dentrassi.pm.common.utils.ThrowingConsumer;

/**
 * A handle to a channel
 * <p>
 * This class represents handle to a channel in a storage service. It can be
 * held for a longer period of time in order to operate on this channel. However
 * the channel might get deleted or modified in the meantime.
 * </p>
 * <p>
 * Channels have to be equal by ID
 * </p>
 */
public interface Channel extends Validated
{
    public String getId ();

    /**
     * A unique alias to the channel id
     *
     * @return the alias name or <code>null</code> if none is set
     */
    public String getName ();

    /**
     * Get either the name or the ID of this channel
     *
     * @return if the name not null or empty the name will returned, otherwise
     *         the ID
     */
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
     * Get the name and id in the format "name (id)"
     * <p>
     * The the channels does not have a name, then the result is the same as
     * {@link #getId()}
     * </p>
     *
     * @return the name and id
     */
    public default String getNameAndId ()
    {
        final String name = getName ();
        if ( name == null )
        {
            return getId ();
        }

        return String.format ( "%s (%s)", name, getId () );
    }

    /**
     * A plain text description of this channel
     *
     * @return the description
     */
    public String getDescription ();

    // -- validation

    /**
     * Get the list of validation messages for this channel
     *
     * @return the list of validation messages, never returns <code>null</code>
     */
    public List<ValidationMessage> getValidationMessages ();

    // -- locks

    /**
     * Test if the channel is locked
     *
     * @return <code>true</code> if the channel is locked, <code>false</code>
     *         otherwise
     */
    public boolean isLocked ();

    /**
     * Lock the channel for further modifications
     * <p>
     * Note that the channel can still be renamed and data can be accessed.
     * However the channel cannot be deleted or any contained artifacts be
     * modified.
     * </p>
     * <p>
     * If the channel is already locked nothing will happen.
     * </p>
     */
    public void lock ();

    /**
     * Reverses a channel lock
     * <p>
     * Reverses the effects of {@link #lock()}
     * </p>
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
    public default Set<String> getAspectIds ()
    {
        return getAspectStates ().keySet ();
    }

    /**
     * Add aspects to the channel
     * <p>
     * If aspects are already present on the channel the operation ignores it
     * </p>
     *
     * @param withDependencies
     *            if the aspects should be added with all required dependencies
     *            pass <code>true</code>
     * @param aspectIds
     *            the aspects to add
     */
    public void addAspects ( boolean withDependencies, String... aspectIds );

    /**
     * Remove aspects from a channel
     * <p>
     * If the aspects are not present on the channel, this operation ignores it
     * </p>
     *
     * @param aspectIds
     *            the aspects to remove
     */
    public void removeAspects ( String... aspectIds );

    public Map<String, String> getAspectStates ();

    /**
     * Check if an aspect is assigned to this channel
     *
     * @param aspectId
     *            the ID of the aspect to check for
     * @return <code>true</code> if the aspect is assigned, <code>false</code>
     *         otherwise
     */
    public boolean hasAspect ( String aspectId );

    public Artifact createArtifact ( String name, InputStream stream, Map<MetaKey, String> providedMetaData );

    /**
     * Get an artifact from a channel by ID
     * <p>
     * If an artifact with the ID exists, but does not belong to this channel,
     * then this method must return <code>null</code>.
     * </p>
     *
     * @param artifactId
     *            the artifact to get
     * @return the artifact or <code>null</code> if the artifact could not be
     *         found
     */
    public Artifact getArtifact ( String artifactId );

    public List<Artifact> findByName ( String artifactName );

    public Set<SimpleArtifactInformation> getSimpleArtifacts ();

    /**
     * Get full information of all artifacts
     * <p>
     * This method behaves exactly like {@link #getSimpleArtifacts()} but
     * provides more details in the returned information. Information is of type
     * {@link ArtifactInformation}, which mainly includes child relations.
     * </p>
     *
     * @return the artifact information
     */
    public Set<ArtifactInformation> getArtifactInformations ();

    /**
     * Get detailed information of all artifacts
     * <p>
     * This method behaves exactly like {@link #getSimpleArtifacts()} but
     * provides more details in the returned information. Information is of type
     * {@link DetailedArtifactInformation}, which mainly includes the meta data.
     * </p>
     *
     * @return the artifact information
     */
    public Set<DetailedArtifactInformation> getDetailedArtifacts ();

    // --- direct meta data

    public SortedMap<MetaKey, String> getMetaData ();

    /**
     * Get a single meta data entry
     *
     * @param key
     *            the qualified key to get
     * @return the value
     */
    public default String getMetaData ( final MetaKey key )
    {
        return getMetaData ().get ( key );
    }

    /**
     * Get a single meta data entry
     *
     * @param namespace
     *            the namespace
     * @param key
     *            the key
     * @return the value
     */
    public default String getMetaData ( final String namespace, final String key )
    {
        return getMetaData ( new MetaKey ( namespace, key ) );
    }

    public SortedMap<MetaKey, String> getProvidedMetaData ();

    public void applyMetaData ( Map<MetaKey, String> metadata );

    // --- Deploy groups and keys

    public Collection<DeployKey> getAllDeployKeys ();

    public Collection<DeployGroup> getDeployGroups ();

    public void addDeployGroup ( String groupId );

    public void removeDeployGroup ( String groupId );

    /**
     * Stream the cache entry to the consumer
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
     * @return <code>true</code> if the cache entry was found,
     *         <code>false</code> otherwise
     */
    public boolean streamCacheEntry ( MetaKey key, ThrowingConsumer<CacheEntry> consumer );

    /**
     * Stream the data of a cache entry to the consumer
     *
     * @param key
     *            the key of the cache entry
     * @param consumer
     *            the consumer of the input stream
     * @throws FileNotFoundException
     *             if the cache entry does not exists
     */
    public default void streamCacheData ( final MetaKey key, final ThrowingConsumer<InputStream> consumer ) throws FileNotFoundException
    {
        streamCacheEntry ( key, entry -> {
            consumer.accept ( entry.getStream () );
        } );
    }

    /**
     * Stream the data of a cache entry directly to an OutputStream
     *
     * @param key
     *            the key of the cache entry
     * @param out
     *            the output stream which will receive the content
     * @throws FileNotFoundException
     *             if the cache entry does not exists
     */
    public default void streamCacheData ( final MetaKey key, final OutputStream out ) throws FileNotFoundException
    {
        streamCacheData ( key, in -> ByteStreams.copy ( in, out ) );
    }

    public default byte[] getCacheEntry ( final MetaKey key ) throws FileNotFoundException
    {
        final Holder<byte[]> holder = new Holder<> ();

        streamCacheEntry ( key, entry -> {
            holder.value = ByteStreams.toByteArray ( entry.getStream () );
        } );

        return holder.value;
    }

    /**
     * Get information about all present cache entries
     *
     * @return the list of information
     */
    public List<CacheEntryInformation> getAllCacheEntries ();

}
