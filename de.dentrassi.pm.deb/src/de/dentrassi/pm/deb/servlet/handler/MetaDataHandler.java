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
import java.util.Base64;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import de.dentrassi.pm.common.MetaKey;

public class MetaDataHandler implements Handler
{
    private final Map<MetaKey, String> metaData;

    private final MetaKey key;

    private final String mimeType;

    public MetaDataHandler ( final Map<MetaKey, String> metaData, final MetaKey key, final String mimeType )
    {
        this.metaData = metaData;
        this.key = key;
        this.mimeType = mimeType;
    }

    @Override
    public void process ( final OutputStream stream ) throws IOException
    {
        final String result = this.metaData.get ( this.key );
        if ( result != null )
        {
            stream.write ( Base64.getDecoder ().decode ( result ) );
        }
    }

    @Override
    public void process ( final HttpServletResponse response ) throws IOException
    {
        final String result = this.metaData.get ( this.key );
        if ( result == null )
        {
            response.setStatus ( HttpServletResponse.SC_NOT_FOUND );
            response.getWriter ().format ( "Content '%s' not found.%n", this.key );
            return;
        }

        final byte[] data = Base64.getDecoder ().decode ( result );

        response.setContentType ( this.mimeType );
        response.setContentLength ( data.length );
        response.getOutputStream ().write ( data );
    }
}
