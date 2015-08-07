package de.dentrassi.pm.storage.channel;

public interface DescriptorAdapter
{
    public ChannelId getDescriptor ();

    public void setName ( String name );

    public default String getName ()
    {
        return getDescriptor ().getName ();
    }
}
