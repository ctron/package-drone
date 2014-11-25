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

public interface StorageService
{
    public Channel createChannel ();

    public Channel getChannel ( String channelId );

    public Artifact createArtifact ( String channelId, String name, InputStream stream );

    public Collection<Channel> listChannels ();

    public void deleteChannel ( String channelId );

    public void streamArtifact ( String artifactId, ArtifactReceiver consumer ) throws FileNotFoundException;

    public ArtifactInformation deleteArtifact ( String artifactId );

    public void addChannelAspect ( String channelId, String aspectFactoryId );

    public void removeChannelAspect ( String channelId, String aspectFactoryId );

    public ArtifactInformation getArtifactInformation ( String artifactId );

    public Artifact getArtifact ( String artifactId );

    public Collection<Artifact> findByName ( String channelId, String format );
}
