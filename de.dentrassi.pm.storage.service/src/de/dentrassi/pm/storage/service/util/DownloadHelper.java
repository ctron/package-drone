/*******************************************************************************
 * Copyright (c) 2014, 2015 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package de.dentrassi.pm.storage.service.util;

import java.io.IOException;
import java.util.function.Function;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.ByteStreams;

import de.dentrassi.pm.common.ArtifactInformation;
import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.storage.Artifact;
import de.dentrassi.pm.storage.service.StorageService;

public final class DownloadHelper
{
    private final static Logger logger = LoggerFactory.getLogger ( DownloadHelper.class );

    private DownloadHelper ()
    {
    }

    public static final String APPLICATION_OCTET_STREAM = "application/octet-stream";

    public static void streamArtifact ( final HttpServletResponse response, final StorageService storageService, final String artifactId, final String mimetype, final boolean download ) throws IOException
    {
        streamArtifact ( response, storageService, artifactId, mimetype, download, ArtifactInformation::getName );
    }

    public static void streamArtifact ( final HttpServletResponse response, final StorageService storageService, final String artifactId, final String mimetype, final boolean download, final Function<ArtifactInformation, String> nameProvider ) throws IOException
    {
        final Artifact artifact = storageService.getArtifact ( artifactId );
        if ( artifact == null )
        {
            response.setStatus ( HttpServletResponse.SC_NOT_FOUND );
            response.setContentType ( "text/plain" );
            response.getWriter ().format ( "Artifact '%s' could not be found", artifactId );
            return;
        }

        streamArtifact ( response, artifact, mimetype, download, nameProvider );
    }

    public static void streamArtifact ( final HttpServletResponse response, final Artifact artifact, final String mimetype, final boolean download ) throws IOException
    {
        streamArtifact ( response, artifact, mimetype, download, ArtifactInformation::getName );
    }

    public static void streamArtifact ( final HttpServletResponse response, final Artifact artifact, final String mimetype, final boolean download, final Function<ArtifactInformation, String> nameProvider ) throws IOException
    {
        artifact.streamData ( ( info, stream ) -> {

            String mt = mimetype;
            if ( mt == null )
            {
                mt = getMimeType ( artifact );
            }

            response.setStatus ( HttpServletResponse.SC_OK );
            response.setContentType ( mt );
            response.setDateHeader ( "Last-Modified", info.getCreationTimestamp ().getTime () );

            try
            {
                response.setContentLengthLong ( info.getSize () );
                if ( download )
                {
                    response.setHeader ( "Content-Disposition", String.format ( "attachment; filename=%s", info.getName () ) );
                }
                final long size = ByteStreams.copy ( stream, response.getOutputStream () );
                logger.debug ( "Copyied {} bytes", size );
            }
            catch ( final Exception e )
            {
                throw new RuntimeException ( e );
            }
        } );
    }

    private static String getMimeType ( final Artifact artifact )
    {
        final String mimetype = artifact.getInformation ().getMetaData ().get ( new MetaKey ( "mime", "type" ) );
        return mimetype == null ? APPLICATION_OCTET_STREAM : mimetype;
    }
}
