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

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import de.dentrassi.osgi.web.RequestHandler;
import de.dentrassi.osgi.web.controller.ControllerInterceptorProcessor;
import de.dentrassi.osgi.web.controller.NoOpRequestHandler;

public abstract class AbstractSecurityControllerInterceptor implements ControllerInterceptorProcessor
{

    protected RequestHandler handleAccessDenied ( final HttpServletResponse response ) throws IOException
    {
        response.setStatus ( HttpServletResponse.SC_FORBIDDEN );
        response.getWriter ().write ( "Access denied" );

        return new NoOpRequestHandler ();
    }

    protected RequestHandler handleLoginRequired ( final HttpServletResponse response ) throws IOException
    {
        response.setStatus ( HttpServletResponse.SC_FORBIDDEN );
        response.getWriter ().write ( "Login required" );

        return new NoOpRequestHandler ();
    }

}
