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
