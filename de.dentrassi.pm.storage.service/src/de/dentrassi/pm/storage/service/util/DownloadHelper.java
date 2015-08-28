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
import java.io.InputStream;
import java.util.function.Function;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.ByteStreams;

import de.dentrassi.pm.common.ArtifactInformation;
import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.storage.Artifact;
import de.dentrassi.pm.storage.channel.ChannelNotFoundException;
import de.dentrassi.pm.storage.channel.ChannelService;
import de.dentrassi.pm.storage.channel.ChannelService.By;
import de.dentrassi.pm.storage.channel.ReadableChannel;
import de.dentrassi.pm.storage.service.StorageService;

public final class DownloadHelper
{
    private final static Logger logger = LoggerFactory.getLogger ( DownloadHelper.class );

    private DownloadHelper ()
    {
    }

    public static final String APPLICATION_OCTET_STREAM = "application/octet-stream";

    public static void streamArtifact ( final HttpServletResponse response, final ChannelService service, final String channelId, final String artifactId, final String mimetype, final boolean download ) throws IOException
    {
        try
        {
            service.access ( By.id ( channelId ), ReadableChannel.class, channel -> {
                final de.dentrassi.pm.storage.channel.ArtifactInformation artifact = channel.getContext ().getArtifacts ().get ( artifactId );
                if ( artifact == null )
                {
                    response.setStatus ( HttpServletResponse.SC_NOT_FOUND );
                    response.setContentType ( "text/plain" );
                    response.getWriter ().format ( "Artifact '%s' in channel '%s' could not be found", artifact, channelId );
                    return;
                }

                if ( !channel.getContext ().stream ( artifactId, stream -> {
                    streamArtifact ( response, artifact, stream, mimetype, download );
                } ) )
                {
                    // failed to stream
                }
            } );
        }
        catch ( final ChannelNotFoundException e )
        {
            response.setStatus ( HttpServletResponse.SC_NOT_FOUND );
            response.setContentType ( "text/plain" );
            response.getWriter ().format ( "Channel '%s' could not be found", channelId );
        }
    }

    private static void streamArtifact ( final HttpServletResponse response, final de.dentrassi.pm.storage.channel.ArtifactInformation artifact, final InputStream stream, final String mimetype, final boolean download ) throws IOException
    {
        // FIXME: detect mime type

        final String mt = mimetype == null ? APPLICATION_OCTET_STREAM : mimetype;

        response.setStatus ( HttpServletResponse.SC_OK );
        response.setContentType ( mt );
        response.setDateHeader ( "Last-Modified", artifact.getCreationInstant ().toEpochMilli () );
        response.setContentLengthLong ( artifact.getSize () );

        if ( download )
        {
            response.setHeader ( "Content-Disposition", String.format ( "attachment; filename=%s", artifact.getName () ) );
        }

        final long size = ByteStreams.copy ( stream, response.getOutputStream () );
        logger.debug ( "Copyied {} bytes", size );
    }

    @Deprecated
    public static void streamArtifact ( final HttpServletResponse response, final StorageService storageService, final String artifactId, final String mimetype, final boolean download ) throws IOException
    {
        streamArtifact ( response, storageService, artifactId, mimetype, download, ArtifactInformation::getName );
    }

    @Deprecated
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

    @Deprecated
    public static void streamArtifact ( final HttpServletResponse response, final Artifact artifact, final String mimetype, final boolean download ) throws IOException
    {
        streamArtifact ( response, artifact, mimetype, download, ArtifactInformation::getName );
    }

    @Deprecated
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
