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
package de.dentrassi.pm.api.upload;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scada.utils.ExceptionHelper;

import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.storage.Artifact;
import de.dentrassi.pm.storage.Channel;
import de.dentrassi.pm.storage.service.servlet.AbstractStorageServiceServlet;

public class UploadServlet extends AbstractStorageServiceServlet
{
    private static final long serialVersionUID = 1L;

    /**
     * Upload by "/channel/<channel>/<artifactName>?ns:key=value"
     */
    @Override
    protected void doPut ( final HttpServletRequest req, final HttpServletResponse resp ) throws ServletException, IOException
    {
        processUpload ( req, resp );
    }

    @Override
    protected void doPost ( final HttpServletRequest req, final HttpServletResponse resp ) throws ServletException, IOException
    {
        processUpload ( req, resp );
    }

    private void processUpload ( final HttpServletRequest req, final HttpServletResponse resp ) throws IOException
    {
        String path = req.getPathInfo ();

        path = path.replaceFirst ( "^/+", "" );
        path = path.replaceFirst ( "/+$", "" );

        final String[] toks = path.split ( "/", 3 );

        if ( toks.length == 0 )
        {
            // handle error: missing channel
            sendError ( resp, HttpServletResponse.SC_BAD_REQUEST, "No target" );
            return;
        }
        if ( toks.length == 1 )
        {
            // handle error: missing channel
            sendError ( resp, HttpServletResponse.SC_BAD_REQUEST, "Missing target" );
            return;
        }
        else if ( toks.length == 2 )
        {
            // handle error: missing name
            sendError ( resp, HttpServletResponse.SC_BAD_REQUEST, "Missing artifact name" );
            return;
        }

        final String targetType = toks[0];

        switch ( targetType )
        {
            case "channel":
                processChannel ( req, resp, toks[1], toks[2] );
                break;
            default:
                sendError ( resp, HttpServletResponse.SC_BAD_REQUEST, "Unkown target type: " + targetType );
                break;
        }
    }

    private void processChannel ( final HttpServletRequest req, final HttpServletResponse resp, final String channelIdOrName, final String artifactName ) throws IOException
    {
        // process

        resp.setContentType ( "text/plain" );

        final Channel channel = getService ( req ).getChannelWithAlias ( channelIdOrName );
        if ( channel == null )
        {
            resp.setStatus ( HttpServletResponse.SC_NOT_FOUND );
            return;
        }

        // authenticate

        if ( !authenticate ( channel, req, resp ) )
        {
            return;
        }

        // do store

        store ( channel, artifactName, req, resp );
    }

    private void sendError ( final HttpServletResponse response, final int status, final String message ) throws IOException
    {
        response.setStatus ( status );
        response.setContentType ( "text/plain" );
        response.getWriter ().println ( message );
    }

    private void store ( final Channel channel, final String name, final HttpServletRequest request, final HttpServletResponse response ) throws IOException
    {
        try
        {
            final Artifact art = channel.createArtifact ( name, request.getInputStream (), makeMetaData ( request ) );

            response.setStatus ( HttpServletResponse.SC_OK );
            if ( art != null )
            {
                // no veto
                response.getWriter ().println ( art.getId () );
            }
        }
        catch ( final IllegalArgumentException e )
        {
            sendError ( response, HttpServletResponse.SC_BAD_REQUEST, ExceptionHelper.getMessage ( e ) );
        }
    }

    private Map<MetaKey, String> makeMetaData ( final HttpServletRequest request )
    {
        final Map<MetaKey, String> result = new HashMap<> ();

        for ( final Map.Entry<String, String[]> entry : request.getParameterMap ().entrySet () )
        {
            final MetaKey key = MetaKey.fromString ( entry.getKey () );
            if ( key == null )
            {
                throw new IllegalArgumentException ( String.format ( "Invalid meta data key format: %s", entry.getKey () ) );
            }

            final String[] values = entry.getValue ();
            if ( values == null || values.length < 1 )
            {
                continue;
            }

            result.put ( key, values[0] );
        }

        return result;
    }
}
