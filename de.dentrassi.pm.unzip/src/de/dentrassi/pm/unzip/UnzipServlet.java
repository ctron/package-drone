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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
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

import de.dentrassi.pm.common.ArtifactInformation;
import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.storage.Artifact;
import de.dentrassi.pm.storage.Channel;
import de.dentrassi.pm.storage.service.servlet.AbstractStorageServiceServlet;
import de.dentrassi.pm.storage.service.util.DownloadHelper;

public class UnzipServlet extends AbstractStorageServiceServlet
{

    private static final MetaKey MK_MIME_TYPE = new MetaKey ( "mime", "type" );

    private static final MetaKey MK_MVN_EXTENSION = new MetaKey ( "mvn", "extension" );

    private static final long serialVersionUID = 1L;

    private final static Logger logger = LoggerFactory.getLogger ( UnzipServlet.class );

    private static final MetaKey MK_GROUP_ID = new MetaKey ( "mvn", "groupId" );

    private static final MetaKey MK_ARTIFACT_ID = new MetaKey ( "mvn", "artifactId" );

    private static final MetaKey MK_VERSION = new MetaKey ( "mvn", "version" );

    private static final MetaKey MK_SNAPSHOT_VERSION = new MetaKey ( "mvn", "snapshotVersion" );

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
                case "newestZip":
                    handleNewestZip ( request, response, path );
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
        handleWithFilter ( "newest", request, response, path, null );
    }

    protected void handleNewestZip ( final HttpServletRequest request, final HttpServletResponse response, final LinkedList<String> path ) throws IOException
    {
        handleWithFilter ( "newestZip", request, response, path, UnzipServlet::isZip );
    }

    private void handleWithFilter ( final String type, final HttpServletRequest request, final HttpServletResponse response, final LinkedList<String> path, final Predicate<Artifact> filter ) throws IOException
    {
        requirePathPrefix ( path, 1, String.format ( "The '%1$s' method requires at least one parameter: channel. e.g. /unzip/%1$s/<channelIdOrName>/path/to/file", type ) );

        final String channelIdOrName = path.pop ();

        final Channel channel = getService ().getChannelWithAlias ( channelIdOrName );
        if ( channel == null )
        {
            throw new IllegalStateException ( String.format ( "Channel with ID or name '%s' not found", channelIdOrName ) );
        }

        List<Artifact> arts = new ArrayList<> ( channel.getArtifacts () );

        if ( filter != null )
        {
            arts = arts.stream ().filter ( filter ).collect ( Collectors.toList () );
        }

        if ( arts.isEmpty () )
        {
            throw new IllegalStateException ( String.format ( "Unable to find artifacts in channel '%s' (%s)", channelIdOrName, channel.getId () ) );
        }

        Collections.sort ( arts, Artifact.CREATION_TIMESTAMP_COMPARATOR );

        final Artifact artifact = arts.get ( 0 );

        logger.debug ( "Streaming artifact {} for channel {}", artifact.getId (), channelIdOrName );

        streamArtifactEntry ( response, artifact.getId (), path );
    }

    /*
    private static final VersionScheme VERSION_SCHEME = new GenericVersionScheme ();

    private void handleMaven ( final String type, final HttpServletRequest request, final HttpServletResponse response, final LinkedList<String> path ) throws IOException
    {
        requirePathPrefix ( path, 1, String.format ( "The '%1$s' method requires at least one parameter: channel. e.g. /unzip/%1$s/<channelIdOrName>/path/to/file", type ) );

        final String channelIdOrName = path.pop ();

        final String groupId = "";
        final String artifactId = "";
        final String version = "";

        final Map<String, List<Artifact>> arts = getMavenArtifacts ( channelIdOrName, groupId, artifactId );

        Version bestVersion = null;
        Artifact bestArtifact = null;

        for ( final Map.Entry<String, List<Artifact>> entry : arts.entrySet () )
        {
            final String vs = entry.getKey ();
            final GenericVersion v;
            try
            {
                v = VERSION_SCHEME.parseVersion ( vs );
            }
            catch ( final InvalidVersionSpecificationException e )
            {
                // ignore this one
                continue;
            }

            // check if version matches

            if ( bestVersion == null || bestVersion.compareTo ( v ) < 0 )
            {
                bestVersion = v;
                Collections.sort ( entry.getValue (), Artifact.CREATION_TIMESTAMP_COMPARATOR );
                bestArtifact = entry.getValue ().get ( 0 );
            }
        }

        / *
        Collections.sort ( arts, Artifact.CREATION_TIMESTAMP_COMPARATOR );

        final Artifact artifact = arts.get ( 0 );

        logger.debug ( "Streaming artifact {} for channel {}", artifact.getId (), channelIdOrName );

        streamArtifactEntry ( response, artifact.getId (), path );
        * /
    }

    */

    protected Map<String, List<Artifact>> getMavenArtifacts ( final String channelIdOrName, final String groupId, final String artifactId )
    {
        final Channel channel = getService ().getChannelWithAlias ( channelIdOrName );
        if ( channel == null )
        {
            throw new IllegalStateException ( String.format ( "Channel with ID or name '%s' not found", channelIdOrName ) );
        }

        final Map<String, List<Artifact>> arts = new HashMap<> ();

        for ( final Artifact art : channel.getArtifacts () )
        {
            if ( !isZip ( art ) )
            {
                continue;
            }

            final ArtifactInformation ai = art.getInformation ();
            final String mvnGroupId = ai.getMetaData ().get ( MK_GROUP_ID );
            final String mvnArtifactId = ai.getMetaData ().get ( MK_ARTIFACT_ID );
            final String mvnVersion = ai.getMetaData ().get ( MK_VERSION );
            final String mvnSnapshotVersion = ai.getMetaData ().get ( MK_SNAPSHOT_VERSION );

            if ( mvnGroupId == null || mvnArtifactId == null || mvnVersion == null )
            {
                continue;
            }

            if ( !mvnGroupId.equals ( groupId ) || !mvnGroupId.equals ( artifactId ) )
            {
                continue;
            }

            addArtifact ( arts, mvnVersion, art );
            if ( mvnSnapshotVersion != null )
            {
                addArtifact ( arts, mvnSnapshotVersion, art );
            }
        }

        if ( arts.isEmpty () )
        {
            throw new IllegalStateException ( String.format ( "Unable to find artifact in channel '%s' (%s)", channelIdOrName, channel.getId () ) );
        }

        return arts;
    }

    private static void addArtifact ( final Map<String, List<Artifact>> arts, final String version, final Artifact artifact )
    {
        List<Artifact> list = arts.get ( version );
        if ( list == null )
        {
            list = new LinkedList<> ();
            arts.put ( version, list );
        }
        list.add ( artifact );
    }

    protected static boolean isZip ( final Artifact art )
    {
        if ( art.getInformation ().getName ().toLowerCase ().endsWith ( ".zip" ) )
        {
            return true;
        }

        final String mdExtension = art.getInformation ().getMetaData ().get ( MK_MVN_EXTENSION );
        if ( mdExtension != null && mdExtension.equalsIgnoreCase ( "zip" ) )
        {
            return true;
        }

        final String mdMime = art.getInformation ().getMetaData ().get ( MK_MIME_TYPE );
        if ( mdMime != null && mdMime.equalsIgnoreCase ( "application/zip" ) )
        {
            return true;
        }

        return false;
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

        final Artifact artifact = arts.get ( 0 );

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
            DownloadHelper.streamArtifact ( response, getService (), artifactId, null, true );
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
