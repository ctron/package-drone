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
package de.dentrassi.pm.storage.web;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.servlet.HandlerMapping;

public class InterceptorHelper
{
    private InterceptorHelper ()
    {
    }

    public static String makeCurrent ( final HttpServletRequest request )
    {
        final Object o = request.getAttribute ( HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE );
        if ( ! ( o instanceof String ) )
        {
            return null;
        }

        final String s = (String)o;

        if ( "/".equals ( s ) )
        {
            return "/";
        }

        if ( s.endsWith ( "/" ) )
        {
            return s.substring ( 0, s.length () - 1 );
        }
        return s;
    }
}
