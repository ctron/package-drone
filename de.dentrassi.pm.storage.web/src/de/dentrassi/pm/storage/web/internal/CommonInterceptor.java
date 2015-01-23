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
package de.dentrassi.pm.storage.web.internal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.framework.FrameworkUtil;

import de.dentrassi.osgi.web.ModelAndView;
import de.dentrassi.osgi.web.RequestHandler;
import de.dentrassi.osgi.web.interceptor.ModelAndViewInterceptorAdapter;

public class CommonInterceptor extends ModelAndViewInterceptorAdapter
{
    private final String version;

    public CommonInterceptor ()
    {
        this.version = FrameworkUtil.getBundle ( CommonInterceptor.class ).getVersion ().toString ();
    }

    @Override
    protected void postHandle ( final HttpServletRequest request, final HttpServletResponse response, final RequestHandler requestHandler, final ModelAndView modelAndView )
    {
        if ( modelAndView != null && !modelAndView.isRedirect () )
        {
            modelAndView.put ( "droneVersion", this.version );
        }
    }
}
