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
package de.dentrassi.pm.storage.web.utils;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.ByteStreams;

import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.storage.Channel;

public class ChannelCacheHandler
{
    private final static Logger logger = LoggerFactory.getLogger ( ChannelCacheHandler.class );

    private final MetaKey key;

    public ChannelCacheHandler ( final MetaKey key )
    {
        this.key = key;
    }

    public void process ( final Channel channel, final HttpServletRequest request, final HttpServletResponse response ) throws IOException
    {
        if ( !channel.streamCacheEntry ( this.key, ( cacheEntry ) -> {
            response.setContentType ( cacheEntry.getMimeType () );
            response.setContentLengthLong ( cacheEntry.getSize () );
            response.setDateHeader ( "Last-Modified", cacheEntry.getTimestamp ().getTime () );
            final long len = ByteStreams.copy ( cacheEntry.getStream (), response.getOutputStream () );
            logger.trace ( "Transfered {} bytes of data from cache entry: {}", len, this.key );
        } ) )
        {
            logger.warn ( "Unable to find channel cache entry: " + this.key );
            response.setStatus ( HttpServletResponse.SC_NOT_FOUND );
            response.setContentType ( "text/plain" );
            response.getWriter ().format ( "Unable to find: %s", request.getRequestURI () );
        }
    }
}
