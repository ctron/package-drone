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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.activation.FileTypeMap;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scada.utils.str.StringHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.ByteStreams;

import de.dentrassi.pm.storage.Artifact;
import de.dentrassi.pm.storage.Channel;
import de.dentrassi.pm.storage.service.servlet.AbstractStorageServiceServlet;
import de.dentrassi.pm.storage.service.util.DownloadHelper;

public class UnzipServlet extends AbstractStorageServiceServlet
{

    private final static Logger logger = LoggerFactory.getLogger ( UnzipServlet.class );

    private static final long serialVersionUID = 1L;

    private FileTypeMap fileTypeMap;

    @Override
    public void init () throws ServletException
    {
        super.init ();
        this.fileTypeMap = FileTypeMap.getDefaultFileTypeMap ();
    }

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
        if ( toks.length < 1 )
        {
            handleNotFound ( request.getPathInfo (), response );
            return;
        }

        final LinkedList<String> path = new LinkedList<> ( Arrays.asList ( toks ) );

        try
        {
            final String type = path.pop ();
            switch ( type )
            {
                case "artifact":
                    handleArtifact ( request, response, path );
                    return;
                case "newest":
                    handleNewest ( request, response, path );
                    return;
                case "newestByName":
                    handleNewestByName ( request, response, path );
                    return;
                default:
                    handleNotFoundError ( response, String.format ( "Unzip target type '%s' unknown.", type ) );
                    return;
            }
        }
        catch ( final IllegalStateException e )
        {
            response.setStatus ( HttpServletResponse.SC_NOT_FOUND );
            response.setContentType ( "text/plain" );
            response.getWriter ().write ( e.getMessage () );
            return;
        }
        catch ( final IllegalArgumentException e )
        {
            response.setStatus ( HttpServletResponse.SC_BAD_GATEWAY );
            response.setContentType ( "text/plain" );
            response.getWriter ().write ( e.getMessage () );
            return;
        }
    }

    protected void handleNewest ( final HttpServletRequest request, final HttpServletResponse response, final LinkedList<String> path ) throws IOException
    {
        requirePathPrefix ( path, 1, "The 'newest' method requires at least one parameter: channel. e.g. /unzip/newest/<channelIdOrName>/path/to/file" );

        final String channelIdOrName = path.pop ();

        final Channel channel = getService ().getChannelWithAlias ( channelIdOrName );
        if ( channel == null )
        {
            throw new IllegalStateException ( String.format ( "Channel with ID or name '%s' not found", channelIdOrName ) );
        }

        final List<Artifact> arts = new ArrayList<> ( channel.getArtifacts () );

        if ( arts.isEmpty () )
        {
            throw new IllegalStateException ( String.format ( "Unable to find artifacts in channel '%s' (%s)", channelIdOrName, channel.getId () ) );
        }

        Collections.sort ( arts, Artifact.CREATION_TIMESTAMP_COMPARATOR );

        final Artifact artifact = arts.listIterator ().next ();

        logger.debug ( "Streaming artifact {} for channel {}", artifact.getId (), channelIdOrName );

        streamArtifactEntry ( response, artifact.getId (), path );
    }

    protected void handleNewestByName ( final HttpServletRequest request, final HttpServletResponse response, final LinkedList<String> path ) throws IOException
    {
        requirePathPrefix ( path, 2, "The 'newestByName' method requires at least two parameters: channel and name. e.g. /unzip/newestByName/<channelIdOrName>/<artifactName>/path/to/file" );

        final String channelIdOrName = path.pop ();
        final String name = path.pop ();

        final Channel channel = getService ().getChannelWithAlias ( channelIdOrName );
        if ( channel == null )
        {
            throw new IllegalStateException ( String.format ( "Channel with ID or name '%s' not found", channelIdOrName ) );
        }

        final List<Artifact> arts = channel.findByName ( name );

        if ( arts.isEmpty () )
        {
            throw new IllegalStateException ( String.format ( "Unable to find artifact with name '%s' in channel '%s' (%s)", name, channelIdOrName, channel.getId () ) );
        }

        Collections.sort ( arts, Artifact.CREATION_TIMESTAMP_COMPARATOR );

        final Artifact artifact = arts.listIterator ().next ();

        logger.debug ( "Streaming artifact {} for name {} in channel {}", artifact.getId (), name, channelIdOrName );

        streamArtifactEntry ( response, artifact.getId (), path );
    }

    private void requirePathPrefix ( final LinkedList<String> path, final int pathPrefixCount, final String message )
    {
        if ( path.size () < pathPrefixCount )
        {
            throw new IllegalArgumentException ( message );
        }
    }

    protected void handleArtifact ( final HttpServletRequest request, final HttpServletResponse response, final LinkedList<String> path ) throws IOException
    {
        requirePathPrefix ( path, 1, "The 'artifact' method requires at least one parameter: artifactId. e.g. /unzip/artifact/<artifactId>/path/to/file" );

        final String artifactId = path.pop ();
        try
        {
            streamArtifactEntry ( response, artifactId, path );
        }
        catch ( final FileNotFoundException e )
        {
            handleNotFoundError ( response, String.format ( "Artifact '%s' could not be found", artifactId ) );
            return;
        }
    }

    protected void streamArtifactEntry ( final HttpServletResponse response, final String artifactId, final List<String> path ) throws IOException
    {
        final String localPath = StringHelper.join ( path, "/" );

        if ( localPath.isEmpty () )
        {
            DownloadHelper.streamArtifact ( response, getService (), artifactId, null, false );
            return;
        }

        // TODO: implement cache

        getService ().streamArtifact ( artifactId, ( ai, stream ) -> {
            final ZipInputStream zis = new ZipInputStream ( stream );
            ZipEntry entry;
            while ( ( entry = zis.getNextEntry () ) != null )
            {
                if ( entry.getName ().equals ( localPath ) )
                {
                    final String type = this.fileTypeMap.getContentType ( entry.getName () );
                    response.setContentType ( type );
                    response.setContentLengthLong ( entry.getSize () );
                    ByteStreams.copy ( zis, response.getOutputStream () );
                    return;
                }
            }
            handleNotFoundError ( response, String.format ( "File entry '%s' could not be found in artifact '%s'", localPath, artifactId ) );
        } );
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
