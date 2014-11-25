package de.dentrassi.pm.storage.service.internal;

import java.util.List;
import java.util.Set;

import de.dentrassi.pm.meta.ChannelAspectInformation;
import de.dentrassi.pm.storage.service.Artifact;
import de.dentrassi.pm.storage.service.ArtifactReceiver;
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

    public void streamData ( final String artifactId, final ArtifactReceiver consumer )
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
}
