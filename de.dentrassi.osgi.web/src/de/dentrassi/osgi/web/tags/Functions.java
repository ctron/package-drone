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
package de.dentrassi.osgi.web.tags;

import javax.servlet.http.HttpServletRequest;

public class Functions
{
    public static String active ( final HttpServletRequest request, final String targetUrl )
    {
        if ( targetUrl == null )
        {
            return "";
        }

        return request.getServletPath ().equals ( targetUrl ) ? "active" : "";
    }

    public static String toFirstUpper ( final String string )
    {
        if ( string == null || string.isEmpty () )
        {
            return string;
        }

        return string.substring ( 0, 1 ).toUpperCase () + string.substring ( 1 );
    }
}
