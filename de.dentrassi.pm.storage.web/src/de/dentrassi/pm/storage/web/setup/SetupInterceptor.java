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
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import de.dentrassi.pm.storage.service.StorageService;
import de.dentrassi.pm.storage.web.InterceptorHelper;

public class SetupInterceptor extends HandlerInterceptorAdapter implements InitializingBean, DisposableBean
{
    private ServiceTracker<StorageService, StorageService> tracker;

    @Override
    public void afterPropertiesSet () throws Exception
    {
        this.tracker = new ServiceTracker<> ( FrameworkUtil.getBundle ( SetupInterceptor.class ).getBundleContext (), StorageService.class, null );
        this.tracker.open ();
    }

    @Override
    public void destroy () throws Exception
    {
        this.tracker.close ();
    }

    @Override
    public boolean preHandle ( final HttpServletRequest request, final HttpServletResponse response, final Object handler ) throws Exception
    {
        final String current = InterceptorHelper.makeCurrent ( request );
        if ( current.startsWith ( "/setup" ) || current.startsWith ( "/resources" ) )
        {
            return super.preHandle ( request, response, handler );
        }

        if ( this.tracker.getService () == null )
        {
            response.sendRedirect ( request.getContextPath () + "/setup" );
            return false;
        }
        else
        {
            return super.preHandle ( request, response, handler );
        }
    }
}
