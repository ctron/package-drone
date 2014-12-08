package de.dentrassi.osgi.validation;

import java.util.ArrayList;
import java.util.List;

import javax.validation.ValidationProviderResolver;
import javax.validation.spi.ValidationProvider;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.ServiceTracker;

public class OsgiValidationProviderTracker implements ValidationProviderResolver
{
    @SuppressWarnings ( "rawtypes" )
    private final ServiceTracker<ValidationProvider, ValidationProvider<?>> tracker;

    public OsgiValidationProviderTracker ()
    {
        final BundleContext context = FrameworkUtil.getBundle ( OsgiValidationProviderTracker.class ).getBundleContext ();

        this.tracker = new ServiceTracker<> ( context, ValidationProvider.class, null );
    }

    public void open ()
    {
        this.tracker.open ();
    }

    public void close ()
    {
        this.tracker.close ();
    }

    @Override
    public List<ValidationProvider<?>> getValidationProviders ()
    {
        return new ArrayList<> ( this.tracker.getTracked ().values () );
    }
}
