package de.dentrassi.pm.meta;

public interface ChannelAspectFactory
{
    public static final String FACTORY_ID = "pm.channel.aspect.id";

    public ChannelAspect createAspect ();
}
