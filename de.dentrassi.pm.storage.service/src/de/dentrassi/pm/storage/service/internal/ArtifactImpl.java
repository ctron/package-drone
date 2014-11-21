package de.dentrassi.pm.storage.service.internal;

import de.dentrassi.pm.storage.service.Artifact;
import de.dentrassi.pm.storage.service.ArtifactReceiver;
import de.dentrassi.pm.storage.service.Channel;

public class ArtifactImpl implements Artifact
{

    private final String id;

    private final ChannelImpl channel;

    private final long size;

    private final String name;

    public ArtifactImpl ( final ChannelImpl channel, final String id, final String name, final long size )
    {
        this.id = id;
        this.channel = channel;
        this.name = name;
        this.size = size;
    }

    @Override
    public String getName ()
    {
        return this.name;
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
    public long getSize ()
    {
        return this.size;
    }

    @Override
    public void streamData ( final ArtifactReceiver consumer )
    {
        this.channel.streamData ( this.id, consumer );
    }

    @Override
    public int compareTo ( final Artifact o )
    {
        if ( o == null )
        {
            return 1;
        }

        return this.id.compareTo ( o.getId () );
    }

}
