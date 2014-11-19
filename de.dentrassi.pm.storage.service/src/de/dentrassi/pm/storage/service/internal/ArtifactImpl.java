package de.dentrassi.pm.storage.service.internal;

import java.io.InputStream;
import java.util.function.Consumer;

import de.dentrassi.pm.storage.service.Artifact;
import de.dentrassi.pm.storage.service.Channel;

public class ArtifactImpl implements Artifact
{

    private final String id;

    private final ChannelImpl channel;

    public ArtifactImpl ( final ChannelImpl channel, final String id )
    {
        this.id = id;
        this.channel = channel;
    }

    @Override
    public Channel getChannel ()
    {
        return this.channel;
    }

    @Override
    public String getId ()
    {
        return this.id;
    }

    @Override
    public void streamData ( final Consumer<InputStream> consumer )
    {
        this.channel.streamData ( this.id, consumer );
    }

}
