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
package de.dentrassi.pm.storage.service.util;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.ByteStreams;

import de.dentrassi.pm.storage.MetaKey;
import de.dentrassi.pm.storage.service.Artifact;
import de.dentrassi.pm.storage.service.StorageService;

public final class DownloadHelper
{
    private final static Logger logger = LoggerFactory.getLogger ( DownloadHelper.class );

    private DownloadHelper ()
    {
    }

    public static final String APPLICATION_OCTET_STREAM = "application/octet-stream";

    public static void streamArtifact ( final HttpServletResponse response, final StorageService storageService, final String artifactId, final String mimetype, final boolean download )
    {
        final Artifact artifact = storageService.getArtifact ( artifactId );
        if ( artifact == null )
        {
            response.setStatus ( HttpServletResponse.SC_NOT_FOUND );
            return;
        }

        streamArtifact ( response, artifact, mimetype, download );
    }

    public static void streamArtifact ( final HttpServletResponse response, final Artifact artifact, final String mimetype, final boolean download )
    {
        artifact.streamData ( ( info, stream ) -> {

            String mt = mimetype;
            if ( mt == null )
            {
                mt = getMimeType ( artifact );
            }

            response.setStatus ( HttpServletResponse.SC_OK );
            response.setContentType ( mt );

            try
            {
                response.setContentLengthLong ( info.getLength () );
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
        final String mimetype = artifact.getMetaData ().get ( new MetaKey ( "mime", "type" ) );
        return mimetype == null ? APPLICATION_OCTET_STREAM : mimetype;
    }
}
