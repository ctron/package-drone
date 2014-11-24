package de.dentrassi.pm.storage.service.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import de.dentrassi.pm.meta.ChannelAspectProcessor;

public class Activator implements BundleActivator
{

    private static Activator INSTANCE;

    private ChannelAspectProcessor channelAspects;

    /*
     * (non-Javadoc)
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    @Override
    public void start ( final BundleContext bundleContext ) throws Exception
    {
        Activator.INSTANCE = this;

        this.channelAspects = new ChannelAspectProcessor ( bundleContext );
    }

    /*
     * (non-Javadoc)
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop ( final BundleContext bundleContext ) throws Exception
    {
        if ( this.channelAspects != null )
        {
            this.channelAspects.close ();
            this.channelAspects = null;
        }

        Activator.INSTANCE = null;
    }

    public static ChannelAspectProcessor getChannelAspects ()
    {
        return Activator.INSTANCE.channelAspects;
    }
}
