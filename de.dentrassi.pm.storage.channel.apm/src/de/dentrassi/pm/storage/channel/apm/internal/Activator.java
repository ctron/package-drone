package de.dentrassi.pm.storage.channel.apm.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import de.dentrassi.pm.aspect.ChannelAspectProcessor;

public class Activator implements BundleActivator
{

    private static Activator INSTANCE;

    private ChannelAspectProcessor processor;

    @Override
    public void start ( final BundleContext context ) throws Exception
    {
        INSTANCE = this;
        this.processor = new ChannelAspectProcessor ( context );
    }

    @Override
    public void stop ( final BundleContext context ) throws Exception
    {
        INSTANCE = null;
        this.processor.close ();
        this.processor = null;
    }

    public static ChannelAspectProcessor getProcessor ()
    {
        return INSTANCE.processor;
    }

}
