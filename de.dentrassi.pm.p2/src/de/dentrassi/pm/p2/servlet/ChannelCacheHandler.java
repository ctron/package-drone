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
package de.dentrassi.pm.p2.servlet;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.io.ByteStreams;

import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.storage.Channel;

public class ChannelCacheHandler
{
    private final MetaKey key;

    public ChannelCacheHandler ( final MetaKey key )
    {
        this.key = key;
    }

    public void process ( final Channel channel, final HttpServletRequest request, final HttpServletResponse response ) throws IOException
    {
        try
        {
            channel.streamCacheEntry ( this.key, ( cacheEntry ) -> {
                response.setContentType ( cacheEntry.getMimeType () );
                response.setContentLengthLong ( cacheEntry.getSize () );
                ByteStreams.copy ( cacheEntry.getStream (), response.getOutputStream () );
            } );
        }
        catch ( final FileNotFoundException e )
        {
            response.setStatus ( HttpServletResponse.SC_NOT_FOUND );
            response.setContentType ( "text/plain" );
            response.getWriter ().format ( "Unable to find: %s", request.getRequestURI () );
        }
    }
}
