/*******************************************************************************
 * Copyright (c) 2014 Jens Reimann.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jens Reimann - initial API and implementation
 *******************************************************************************/
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
