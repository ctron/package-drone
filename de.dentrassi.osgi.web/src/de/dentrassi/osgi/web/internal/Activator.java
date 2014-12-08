package de.dentrassi.osgi.web.internal;

import javax.validation.ValidationProviderResolver;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import de.dentrassi.osgi.validation.OsgiValidationProviderTracker;

public class Activator implements BundleActivator
{
    public static Activator INSTANCE;

    private OsgiValidationProviderTracker validationTracker;

    @Override
    public void start ( final BundleContext context ) throws Exception
    {
        this.validationTracker = new OsgiValidationProviderTracker ();
        this.validationTracker.open ();
        INSTANCE = this;
    }

    @Override
    public void stop ( final BundleContext context ) throws Exception
    {
        INSTANCE = null;
        this.validationTracker.close ();
    }

    public static ValidationProviderResolver getValidationProviderResolver ()
    {
        return INSTANCE.validationTracker;
    }
}
