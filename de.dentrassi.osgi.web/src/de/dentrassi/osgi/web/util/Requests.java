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
package de.dentrassi.osgi.web.util;

import javax.servlet.http.HttpServletRequest;

public final class Requests
{
    public static final String IF_MODIFIED_SINCE = "If-Modified-Since";

    private Requests ()
    {
    }

    public static boolean isNotModified ( final HttpServletRequest request, final long lastModified )
    {
        if ( lastModified < 0 )
        {
            return false;
        }

        final long modifiedSince = getIfModifiedSince ( request );
        if ( modifiedSince <= 0 )
        {
            return false;
        }

        return lastModified >= modifiedSince;
    }

    private static long getIfModifiedSince ( final HttpServletRequest request )
    {
        try
        {
            return request.getDateHeader ( IF_MODIFIED_SINCE );
        }
        catch ( final Exception e )
        {
            return -1;
        }
    }
}
