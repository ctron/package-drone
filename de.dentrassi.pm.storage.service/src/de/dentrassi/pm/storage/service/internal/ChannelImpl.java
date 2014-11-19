package de.dentrassi.pm.storage.service.internal;

import java.io.InputStream;
import java.util.Set;
import java.util.function.Consumer;

import de.dentrassi.pm.storage.service.Artifact;
import de.dentrassi.pm.storage.service.Channel;

public class ChannelImpl implements Channel
{
    private final String id;

    private final StorageServiceImpl service;

    public ChannelImpl ( final String id, final StorageServiceImpl service )
    {
        this.id = id;
        this.service = service;
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

    public void streamData ( final String artifactId, final Consumer<InputStream> consumer )
    {
        this.service.streamData ( artifactId, consumer );
    }
}
