/*******************************************************************************
 * Copyright (c) 2015 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package de.dentrassi.pm.storage.web.internal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.dentrassi.osgi.web.ModelAndView;
import de.dentrassi.osgi.web.RequestHandler;
import de.dentrassi.osgi.web.interceptor.ModelAndViewInterceptorAdapter;
import de.dentrassi.pm.VersionInformation;

public class CommonInterceptor extends ModelAndViewInterceptorAdapter
{
    @Override
    protected void postHandle ( final HttpServletRequest request, final HttpServletResponse response, final RequestHandler requestHandler, final ModelAndView modelAndView )
    {
        if ( modelAndView != null && !modelAndView.isRedirect () )
        {
            modelAndView.put ( "droneVersion", VersionInformation.VERSION );
        }
    }
}
