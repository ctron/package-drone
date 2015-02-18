/*******************************************************************************
 * Copyright (c) 2015 Jens Reimann.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jens Reimann - initial API and implementation
 *******************************************************************************/
package de.dentrassi.osgi.job.web.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import de.dentrassi.osgi.job.JobManager;

public class Activator implements BundleActivator
{
    private static Activator INSTANCE;

    private ServiceTracker<JobManager, JobManager> tracker;

    @Override
    public void start ( final BundleContext context ) throws Exception
    {
        this.tracker = new ServiceTracker<JobManager, JobManager> ( context, JobManager.class, null );
        this.tracker.open ();

        INSTANCE = this;
    }

    @Override
    public void stop ( final BundleContext context ) throws Exception
    {
        INSTANCE = null;
        this.tracker.close ();
    }

    public static JobManager getJobManager ()
    {
        return INSTANCE.tracker.getService ();
    }

}
