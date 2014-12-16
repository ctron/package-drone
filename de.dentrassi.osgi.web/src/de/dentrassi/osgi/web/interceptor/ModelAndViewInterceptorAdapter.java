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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.dentrassi.osgi.web.ModelAndView;
import de.dentrassi.osgi.web.RequestHandler;
import de.dentrassi.osgi.web.controller.ModelAndViewRequestHandler;

public abstract class ModelAndViewInterceptorAdapter extends InterceptorAdapter
{
    @Override
    public void postHandle ( final HttpServletRequest request, final HttpServletResponse response, final RequestHandler requestHandler ) throws Exception
    {
        if ( requestHandler instanceof ModelAndViewRequestHandler )
        {
            postHandle ( request, response, requestHandler, ( (ModelAndViewRequestHandler)requestHandler ).getModelAndView () );
        }
        super.postHandle ( request, response, requestHandler );
    }

    protected void postHandle ( final HttpServletRequest request, final HttpServletResponse response, final RequestHandler requestHandler, final ModelAndView modelAndView ) throws Exception
    {
    }
}
