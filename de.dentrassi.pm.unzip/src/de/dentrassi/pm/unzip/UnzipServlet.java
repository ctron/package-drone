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
package de.dentrassi.pm.unzip;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scada.utils.str.StringHelper;

import com.google.common.io.ByteStreams;

public class UnzipServlet extends StorageServiceServlet
{
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet ( final HttpServletRequest request, final HttpServletResponse response ) throws ServletException, IOException
    {
        String pathString = request.getPathInfo ();
        if ( pathString == null )
        {
            handleNotFound ( "", response );
            return;
        }

        if ( pathString.startsWith ( "/" ) )
        {
            pathString = pathString.substring ( 1 );
        }

        final String[] toks = pathString.split ( "/" );
        if ( toks.length < 2 )
        {
            handleNotFound ( request.getPathInfo (), response );
            return;
        }

        final LinkedList<String> path = new LinkedList<> ( Arrays.asList ( toks ) );

        final String type = path.pop ();
        switch ( type )
        {
            case "artifact":
                handleArtifact ( request, response, path );
                return;
            default:
                handleNotFoundError ( response, String.format ( "Unzip target type '%s' unknown.", type ) );
                return;
        }
    }

    protected void handleArtifact ( final HttpServletRequest request, final HttpServletResponse response, final LinkedList<String> path ) throws IOException
    {
        final String artifactId = path.pop ();
        try
        {
            final String localPath = StringHelper.join ( path, "/" );

            getService ().streamArtifact ( artifactId, ( ai, stream ) -> {
                final ZipInputStream zis = new ZipInputStream ( stream );
                ZipEntry entry;
                while ( ( entry = zis.getNextEntry () ) != null )
                {
                    if ( entry.getName ().equals ( localPath ) )
                    {
                        response.setContentType ( "application/octet-stream" );
                        response.setContentLengthLong ( entry.getSize () );
                        ByteStreams.copy ( zis, response.getOutputStream () );
                        return;
                    }
                }
                handleNotFoundError ( response, String.format ( "File entry '%s' could not be found in artifact '%s'", localPath, artifactId ) );
            } );

        }
        catch ( final FileNotFoundException e )
        {
            handleNotFoundError ( response, String.format ( "Artifact '%s' could not be found", artifactId ) );
            return;
        }
    }

    protected void handleNotFound ( final String path, final HttpServletResponse response ) throws IOException
    {
        handleNotFoundError ( response, String.format ( "Resource '%s' cound not be found", path ) );
    }

    protected void handleNotFoundError ( final HttpServletResponse response, final String message ) throws IOException
    {
        response.setStatus ( HttpServletResponse.SC_NOT_FOUND );
        response.setContentType ( "text/plain" );
        response.getWriter ().write ( message );
    }
}
