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
package de.dentrassi.osgi.web.interceptor;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

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
