package de.dentrassi.pm.storage.channel;

public class ChannelArtifactInformation extends ArtifactInformation
{
    private final ChannelId channelId;

    public ChannelArtifactInformation ( final ChannelId channelId, final ArtifactInformation artifactInformation )
    {
        super ( artifactInformation );
        this.channelId = channelId;
    }

    public ChannelId getChannelId ()
    {
        return this.channelId;
    }

}
