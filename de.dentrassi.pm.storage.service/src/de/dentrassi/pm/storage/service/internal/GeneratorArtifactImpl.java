package de.dentrassi.pm.storage.service.internal;

import java.util.Date;
import java.util.Map;

import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.storage.GeneratorArtifact;

public class GeneratorArtifactImpl extends ArtifactImpl implements GeneratorArtifact
{
    public GeneratorArtifactImpl ( final ChannelImpl channel, final String id, final String name, final long size, final Map<MetaKey, String> metaData, final Date creationTimestamp )
    {
        super ( channel, id, name, size, metaData, creationTimestamp, false, true );
    }

    @Override
    public void generate ()
    {
        this.channel.generate ( this.id );
    }

}
