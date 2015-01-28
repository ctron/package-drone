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
package de.dentrassi.pm.sec.web.controller;

import java.lang.reflect.Method;
import java.security.Principal;
import java.util.function.BiFunction;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dentrassi.osgi.web.RequestHandler;
import de.dentrassi.osgi.web.controller.ControllerInterceptorProcessor;
import de.dentrassi.osgi.web.controller.NoOpRequestHandler;

public class SecuredControllerInterceptor implements ControllerInterceptorProcessor
{

    private final static Logger logger = LoggerFactory.getLogger ( SecuredControllerInterceptor.class );

    @Override
    public RequestHandler before ( final Object controller, final Method m, final HttpServletRequest request, final HttpServletResponse response, final BiFunction<HttpServletRequest, HttpServletResponse, RequestHandler> next ) throws Exception
    {
        Secured s = m.getAnnotation ( Secured.class );
        if ( s == null )
        {
            s = controller.getClass ().getAnnotation ( Secured.class );
        }

        logger.trace ( "Checking secured: {} for {}", s, request );

        if ( s == null )
        {
            return next.apply ( request, response );
        }

        final Principal p = request.getUserPrincipal ();

        logger.trace ( "Principal: {}", p );

        if ( p == null && s.value () )
        {
            // anonymous - but not allowed

            response.setStatus ( HttpServletResponse.SC_FORBIDDEN );
            response.getWriter ().write ( "Access denied" );

            return new NoOpRequestHandler ();
        }

        return next.apply ( request, response );
    }
}
