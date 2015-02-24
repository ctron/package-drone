/*******************************************************************************
 * Copyright (c) 2014 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package de.dentrassi.osgi.web;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.io.ByteStreams;

import de.dentrassi.osgi.web.util.Requests;
import de.dentrassi.osgi.web.util.Responses;

public class ResourceRequestHandler implements RequestHandler
{
    private final URL url;

    private final long lastModified;

    public ResourceRequestHandler ( final URL url, final long lastModified )
    {
        this.url = url;
        this.lastModified = lastModified;
    }

    @Override
    public void process ( final HttpServletRequest request, final HttpServletResponse response ) throws IOException
    {
        boolean isGet = request.getMethod ().equalsIgnoreCase ( "get" );

        if ( !isGet && request.getMethod ().equalsIgnoreCase ( "head" ) )
        {
            if ( Requests.isNotModified ( request, this.lastModified ) )
            {
                return;
            }
            isGet = true; // handle as GET
        }

        if ( !isGet )
        {
            Responses.methodNotAllowed ( request, response );
            return;
        }

        response.setDateHeader ( Responses.LAST_MODIFIED, this.lastModified );

        try ( InputStream in = this.url.openStream () )
        {
            ByteStreams.copy ( in, response.getOutputStream () );
        }
    }
}
