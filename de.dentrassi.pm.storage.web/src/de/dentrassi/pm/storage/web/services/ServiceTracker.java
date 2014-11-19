package de.dentrassi.pm.storage.web.services;

import org.osgi.framework.BundleContext;

import de.dentrassi.pm.storage.service.StorageService;

public class ServiceTracker
{
    private final org.osgi.util.tracker.ServiceTracker<StorageService, StorageService> storageTracker;

    public ServiceTracker ( final BundleContext context )
    {
        this.storageTracker = new org.osgi.util.tracker.ServiceTracker<StorageService, StorageService> ( context, StorageService.class, null );
    }

    public void open ()
    {
        this.storageTracker.open ();
    }

    public void close ()
    {
        this.storageTracker.close ();
    }

    public StorageService getStorageService ()
    {
        return this.storageTracker.getService ();
    }

    public StorageService waitForStorageService ( final long timeout )
    {
        try
        {
            return this.storageTracker.waitForService ( timeout );
        }
        catch ( final InterruptedException e )
        {
            return null;
        }
    }

}
