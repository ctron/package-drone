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
}
