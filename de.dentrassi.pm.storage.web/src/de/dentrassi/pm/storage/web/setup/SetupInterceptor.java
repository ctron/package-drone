/*******************************************************************************
 * Copyright (c) 2014, 2015 Jens Reimann.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jens Reimann - initial API and implementation
 *******************************************************************************/
package de.dentrassi.pm.storage.web.setup;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.ServiceTracker;

import de.dentrassi.osgi.web.interceptor.ModelAndViewInterceptorAdapter;
import de.dentrassi.pm.storage.service.StorageService;

public class SetupInterceptor extends ModelAndViewInterceptorAdapter
{
    private ServiceTracker<StorageService, StorageService> tracker;

    private final Set<String> ignoredPrefixes = new HashSet<> ();

    public SetupInterceptor ()
    {
        this.ignoredPrefixes.add ( "/setup" );
        this.ignoredPrefixes.add ( "/login" );
        this.ignoredPrefixes.add ( "/logout" );
        this.ignoredPrefixes.add ( "/resources" );
    }

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

        for ( final String prefix : this.ignoredPrefixes )
        {
            if ( current.startsWith ( prefix ) )
            {
                return super.preHandle ( request, response );
            }
        }

        if ( current.startsWith ( "/config" ) && request.getUserPrincipal () != null )
        {
            // this is a special case where the user is logged in, but the system might not be fully functionaly. In this case let him pass.
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
