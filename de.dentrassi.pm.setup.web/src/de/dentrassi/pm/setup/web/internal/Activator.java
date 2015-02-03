package de.dentrassi.pm.setup.web.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

public class Activator implements BundleActivator
{

    private ServiceTracker<?, ?> tracker;

    private static Activator INSTANCE;

    @Override
    public void start ( final BundleContext context ) throws Exception
    {
        this.tracker = new ServiceTracker<Object, Object> ( context, "de.dentrassi.pm.storage.service.StorageService", null );
        this.tracker.open ( true );
        INSTANCE = this;
    }

    @Override
    public void stop ( final BundleContext context ) throws Exception
    {
        INSTANCE = null;
        this.tracker.close ();
        this.tracker = null;
    }

    public static ServiceTracker<?, ?> getTracker ()
    {
        return INSTANCE.tracker;
    }

}
