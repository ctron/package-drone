package de.dentrassi.osgi.web.interceptor;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import de.dentrassi.osgi.web.Interceptor;

public class InterceptorTracker implements InterceptorLocator
{
    private final ServiceTracker<Interceptor, Interceptor> tracker;

    public InterceptorTracker ( final BundleContext context )
    {
        this.tracker = new ServiceTracker<> ( context, Interceptor.class, null );
        this.tracker.open ();
    }

    @Override
    public void close ()
    {
        this.tracker.close ();
    }

    @Override
    public Interceptor[] getInterceptors ()
    {
        return this.tracker.getServices ( new Interceptor[0] );
    }
}
