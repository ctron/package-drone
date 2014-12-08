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
package de.dentrassi.pm.storage.web.setup;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.ServiceTracker;

import de.dentrassi.osgi.web.ModelAndViewInterceptorAdapter;
import de.dentrassi.pm.storage.service.StorageService;

public class SetupInterceptor extends ModelAndViewInterceptorAdapter
{
    private ServiceTracker<StorageService, StorageService> tracker;

    public void activate ()
    {
        this.tracker = new ServiceTracker<> ( FrameworkUtil.getBundle ( SetupInterceptor.class ).getBundleContext (), StorageService.class, null );
        this.tracker.open ();
    }

    public void deactivate ()
    {
        this.tracker.close ();
    }

    @Override
    public boolean preHandle ( final HttpServletRequest request, final HttpServletResponse response ) throws Exception
    {
        final String current = request.getServletPath ();

        if ( current.startsWith ( "/setup" ) || current.startsWith ( "/resources" ) )
        {
            return super.preHandle ( request, response );
        }

        if ( this.tracker.getService () == null )
        {
            response.sendRedirect ( request.getContextPath () + "/setup" );
            return false;
        }
        else
        {
            return super.preHandle ( request, response );
        }
    }
}
