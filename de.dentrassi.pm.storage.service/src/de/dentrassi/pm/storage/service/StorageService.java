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
package de.dentrassi.pm.storage.service;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

import de.dentrassi.pm.common.ArtifactInformation;
import de.dentrassi.pm.common.MetaKey;

public interface StorageService
{
    public Channel createChannel ();

    public Channel getChannel ( String channelId );

    public Channel getChannelWithAlias ( String channelIdOrName );

    /**
     * Create a new artifact in the channel
     * <p>
     * <em>Note:</em> This action might get vetoed by a channel aspect. In this
     * case the return value is <code>null</code>
     * </p>
     *
     * @param channelId
     *            the channel were to add the artifact
     * @param name
     *            the name of the artifact
     * @param stream
     *            the data stream
     * @param providedMetaData
     *            metadata that was initially provided
     * @return the newly created artifact or <code>null</code> if the creation
     *         was vetoed
     */
    public Artifact createArtifact ( String channelId, String name, InputStream stream, Map<MetaKey, String> providedMetaData );

    public Artifact createGeneratedArtifact ( String channelId, String name, String generatorId, InputStream stream, Map<MetaKey, String> providedMetaData );

    public Collection<Channel> listChannels ();

    public void deleteChannel ( String channelId );

    public void streamArtifact ( String artifactId, ArtifactReceiver consumer ) throws FileNotFoundException;

    public ArtifactInformation deleteArtifact ( String artifactId );

    public void addChannelAspect ( String channelId, String aspectFactoryId );

    public void removeChannelAspect ( String channelId, String aspectFactoryId );

    public ArtifactInformation getArtifactInformation ( String artifactId );

    public Artifact getArtifact ( String artifactId );

    public void clearChannel ( String id );

    public void updateChannel ( String id, String name );
}
