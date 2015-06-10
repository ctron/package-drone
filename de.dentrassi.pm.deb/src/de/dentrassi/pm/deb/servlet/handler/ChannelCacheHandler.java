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
package de.dentrassi.pm.deb.servlet.handler;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import com.google.common.io.ByteStreams;

import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.storage.Channel;

public class ChannelCacheHandler implements Handler
{
    private final Channel channel;

    private final MetaKey key;

    public ChannelCacheHandler ( final Channel channel, final MetaKey key )
    {
        this.channel = channel;
        this.key = key;
    }

    @Override
    public void process ( final OutputStream stream ) throws IOException
    {
        this.channel.streamCacheData ( this.key, stream );
    }

    @Override
    public void process ( final HttpServletResponse response ) throws IOException
    {
        if ( !this.channel.streamCacheEntry ( this.key, entry -> {
            response.setContentType ( entry.getMimeType () );
            response.setContentLengthLong ( entry.getSize () );
            ByteStreams.copy ( entry.getStream (), response.getOutputStream () );
        } ) )
        {
            response.setStatus ( HttpServletResponse.SC_NOT_FOUND );
            response.getWriter ().format ( "Content '%s' not found.%n", this.key );
        }
    }
}
