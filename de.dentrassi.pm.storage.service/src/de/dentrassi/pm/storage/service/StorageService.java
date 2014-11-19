package de.dentrassi.pm.storage.service;

import java.io.InputStream;
import java.util.Collection;

public interface StorageService
{
    public Channel createChannel ();

    public Channel getChannel ( String channelId );

    public Artifact createArtifact ( String channelId, InputStream stream );

    public Collection<Channel> listChannels ();

    public void deleteChannel ( String channelId );
}
