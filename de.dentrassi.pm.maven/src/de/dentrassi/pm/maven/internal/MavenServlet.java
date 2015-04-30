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
package de.dentrassi.pm.maven.internal;

import static de.dentrassi.osgi.web.util.BasicAuthentication.parseAuthorization;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scada.utils.io.RecursiveDeleteVisitor;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;

import de.dentrassi.osgi.web.util.BasicAuthentication;
import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.common.MetaKeys;
import de.dentrassi.pm.common.XmlHelper;
import de.dentrassi.pm.maven.ChannelData;
import de.dentrassi.pm.maven.MavenInformation;
import de.dentrassi.pm.storage.Artifact;
import de.dentrassi.pm.storage.Channel;
import de.dentrassi.pm.storage.DeployKey;
import de.dentrassi.pm.storage.service.StorageService;
import de.dentrassi.pm.storage.web.utils.ChannelCacheHandler;

public class MavenServlet extends HttpServlet
{
    private final static Logger logger = LoggerFactory.getLogger ( MavenServlet.class );

    private static final long serialVersionUID = 1L;

    private ServiceTracker<StorageService, StorageService> tracker;

    private XmlHelper xml;

    private Path tempRoot;

    @Override
    public void init () throws ServletException
    {
        super.init ();

        try
        {
            this.tempRoot = Files.createTempDirectory ( "m2-upload" );
        }
        catch ( final IOException e )
        {
            throw new ServletException ( "Failed to create temp root", e );
        }

        this.xml = new XmlHelper ();

        final BundleContext context = FrameworkUtil.getBundle ( MavenServlet.class ).getBundleContext ();
        this.tracker = new ServiceTracker<> ( context, StorageService.class, null );
        this.tracker.open ();
    }

    @Override
    public void destroy ()
    {
        try
        {
            deleteTemp ();
        }
        catch ( final IOException e )
        {
            logger.warn ( "Failed to clean up temp directory: " + this.tempRoot, e );
        }

        this.tracker.close ();
        super.destroy ();
    }

    private void deleteTemp () throws IOException
    {
        Files.walkFileTree ( this.tempRoot, new RecursiveDeleteVisitor () );
        Files.deleteIfExists ( this.tempRoot );
    }

    @Override
    protected void doGet ( final HttpServletRequest request, final HttpServletResponse response ) throws ServletException, IOException
    {
        logger.trace ( "get request - {}", request );

        if ( "/".equals ( request.getPathInfo () ) )
        {
            response.getWriter ().write ( "Package Drone Maven 2 Repository Adapter" );
            response.setContentType ( "text/plain" );
            response.setStatus ( HttpServletResponse.SC_OK );
            return;
        }

        String pathString = request.getPathInfo ();

        final StorageService service = this.tracker.getService ();

        if ( service == null )
        {
            response.getWriter ().write ( "System not operational" );
            response.setContentType ( "text/plain" );
            response.setStatus ( HttpServletResponse.SC_SERVICE_UNAVAILABLE );
            return;
        }

        pathString = pathString.replaceAll ( "^/+", "" );
        pathString = pathString.replaceAll ( "/+$", "" );

        final String[] toks = pathString.split ( "/+", 2 );
        final String channelId = toks[0];

        final Channel channel = service.getChannelWithAlias ( channelId );
        if ( channel == null )
        {
            response.setStatus ( HttpServletResponse.SC_NOT_FOUND );
            response.setContentType ( "text/plain" );
            response.getWriter ().format ( "Channel %s not found", channelId );
            return;
        }

        final SortedMap<MetaKey, String> md = channel.getMetaData ();
        final String str = md.get ( new MetaKey ( "maven.repo", "channel" ) );
        if ( str == null )
        {
            commitNotConfigured ( response, channelId );
            return;
        }

        final ChannelData channelData;
        try
        {
            channelData = ChannelData.fromJson ( str );
        }
        catch ( final Exception e )
        {
            logger.debug ( "Failed to load maven channel data", e );

            response.getWriter ().write ( "Corrupt channel data" );
            response.setStatus ( HttpServletResponse.SC_SERVICE_UNAVAILABLE );

            return;
        }

        if ( channelData == null )
        {
            logger.debug ( "No maven channel data: {}", channel.getId () );
            commitNotConfigured ( response, channelId );
            return;
        }

        if ( toks.length == 2 && toks[1].equals ( ".meta/repository-metadata.xml" ) )
        {
            new ChannelCacheHandler ( new MetaKey ( "maven.repo", "repo-metadata" ) ).process ( channel, request, response );
            return;
        }

        if ( toks.length == 2 && toks[1].equals ( ".meta/prefixes.txt" ) )
        {
            new ChannelCacheHandler ( new MetaKey ( "maven.repo", "prefixes" ) ).process ( channel, request, response );
            return;
        }

        new MavenHandler ( service, channelData ).handle ( toks.length > 1 ? toks[1] : null, request, response );
    }

    protected void commitNotConfigured ( final HttpServletResponse response, final String channelId ) throws IOException
    {
        response.setStatus ( HttpServletResponse.SC_NOT_FOUND );
        response.setContentType ( "text/plain" );
        response.getWriter ().format ( "Channel %s is not configured for providing a Maven 2 repository. Add the Maven Repository aspect!", channelId );
    }

    @Override
    protected void doPut ( final HttpServletRequest request, final HttpServletResponse response ) throws ServletException, IOException
    {
        try
        {
            final StorageService service = this.tracker.getService ();

            if ( service == null )
            {
                response.getWriter ().write ( "System not operational" );
                response.setStatus ( HttpServletResponse.SC_SERVICE_UNAVAILABLE );
                return;
            }

            logger.debug ( "Request - pathInfo: {} ", request.getPathInfo () );

            processPut ( request, response, service );
        }
        catch ( final IOException e )
        {
            throw e;
        }
        catch ( final Exception e )
        {
            throw new ServletException ( e );
        }
    }

    private void processPut ( final HttpServletRequest request, final HttpServletResponse response, final StorageService service ) throws Exception
    {
        final String[] toks = request.getPathInfo ().split ( "/+" );
        final String channelId = toks[1];
        final String artifactName = toks[toks.length - 1];

        logger.debug ( "Channel: {}, Artifact: {}", channelId, artifactName );

        final Channel channel = service.getChannelWithAlias ( channelId );
        if ( channel == null )
        {
            response.setStatus ( HttpServletResponse.SC_NOT_FOUND );
            response.getWriter ().format ( "Channel %s not found", channelId );
            return;
        }

        if ( !authenticate ( channel, request, response ) )
        {
            return;
        }

        if ( isUpload ( toks, artifactName ) )
        {
            processUpload ( request, toks, artifactName, channel );
        }
        else if ( isMetaData ( toks, artifactName ) )
        {
            processMetaData ( channel, toks, request );
        }
        else if ( isChecksum ( toks, artifactName ) )
        {
            //  validateChecksum ( channel, toks, artifactName, request, response );
        }
        else
        {
            dumpSkip ( request );
        }
        response.setStatus ( HttpServletResponse.SC_OK );
    }

    protected void processUpload ( final HttpServletRequest request, final String[] toks, final String artifactName, final Channel channel ) throws Exception
    {
        final String groupId = getGroupId ( toks );
        final String artifactId = getArtifactId ( toks );
        final String version = getVersion ( toks );

        if ( version.endsWith ( "-SNAPSHOT" ) )
        {
            storeTmp ( channel.getId (), groupId, artifactId, version, artifactName, request );
        }
        else
        {
            final MavenInformation info = detect ( groupId, artifactId, version, artifactName );

            if ( info == null )
            {
                logger.debug ( "Ignoring: {}", request.getPathInfo () );
                return;
            }

            final Artifact parent = getParent ( channel, info.makePlainName () );
            storeArtifact ( channel, info, parent, request.getInputStream () );
        }
    }

    private void storeTmp ( final String channelId, final String groupId, final String artifactId, final String version, final String artifactName, final HttpServletRequest request ) throws IOException
    {
        final File file = new File ( this.tempRoot.toFile (), String.format ( "%s/%s/%s/%s/%s", channelId, groupId, artifactId, version, artifactName ) );
        logger.debug ( "Temp store artifact: {}", file );

        file.delete ();
        file.getParentFile ().mkdirs ();

        try ( OutputStream os = new BufferedOutputStream ( new FileOutputStream ( file ) ) )
        {
            ByteStreams.copy ( request.getInputStream (), os );
        }
    }

    private void validateChecksum ( final Channel channel, final String[] toks, final String artifactName, final HttpServletRequest request, final HttpServletResponse response ) throws IOException
    {
        if ( !channel.hasAspect ( "hasher" ) )
        {
            logger.debug ( "Ignoring checksum on channel: {}", channel.getId () );
            return;
        }

        final String hash = CharStreams.toString ( request.getReader () );

        final String parentName = artifactName.substring ( 0, artifactName.length () - ".sha1".length () );

        logger.info ( "Validate checksum - {} ({} -> {})", hash, artifactName, parentName );

        if ( validHash ( channel, hash, parentName ) )
        {
            logger.debug ( "Checksum valid" );
            return;
        }

        logger.info ( "Invalid checksum" );
        response.setStatus ( HttpServletResponse.SC_CONFLICT );
        response.getWriter ().write ( "Invalid checksum" );
    }

    private boolean validHash ( final Channel channel, final String hash, final String parentName )
    {
        final Collection<Artifact> result = channel.findByName ( parentName );
        for ( final Artifact art : result )
        {
            final String artHash = getHash ( art );
            if ( artHash == null )
            {
                continue;
            }
            logger.debug ( "Checking hash - {} = {}, vs {}", art.getId (), artHash, hash );
            if ( artHash.equalsIgnoreCase ( hash ) )
            {
                return true;
            }
        }

        return false;
    }

    private String getHash ( final Artifact art )
    {
        return art.getInformation ().getMetaData ().get ( new MetaKey ( "hasher", "sha1" ) );
    }

    private boolean isChecksum ( final String[] toks, final String artifactName )
    {
        if ( artifactName.endsWith ( ".sha1" ) )
        {
            return true;
        }
        return false;
    }

    private Artifact getParent ( final Channel channel, final String parentName )
    {
        logger.debug ( "Looking for parent as: '{}'", parentName );
        final Collection<Artifact> result = channel.findByName ( parentName );
        if ( result != null && result.size () == 1 )
        {
            return result.iterator ().next ();
        }

        return null;
    }

    private void processMetaData ( final Channel channel, final String[] toks, final HttpServletRequest request ) throws Exception
    {
        final String groupId = join ( toks, 2, -3 );
        final String artifactId = toks[toks.length - 3];
        final String version = toks[toks.length - 2];

        final Document doc = this.xml.parse ( request.getInputStream () );

        System.out.println ( "----------------------" );
        this.xml.write ( doc, System.out );
        System.out.println ();
        System.out.println ( "----------------------" );

        final Element de = doc.getDocumentElement ();
        if ( !de.getNodeName ().equals ( "metadata" ) )
        {
            return;
        }

        final String releaseVersion = this.xml.getElementValue ( de, "versioning/release" );

        if ( releaseVersion != null )
        {
            // we don't handle metadata for releases
            return;
        }
        else
        {
            final String snapshotTimestamp = this.xml.getElementValue ( de, "versioning/snapshot/timestamp" );
            final String snapshotBuildNumber = this.xml.getElementValue ( de, "versioning/snapshot/buildNumber" );

            Long buildNumber = null;
            if ( snapshotBuildNumber != null )
            {
                buildNumber = Long.parseLong ( snapshotBuildNumber );
            }

            if ( snapshotTimestamp != null && snapshotBuildNumber != null )
            {
                // snapshot version
                System.out.format ( "\t%s %s%n", snapshotTimestamp, snapshotBuildNumber );

                final List<MavenInformation> plain = new LinkedList<> ();
                final List<MavenInformation> classified = new LinkedList<> ();

                for ( final Node node : XmlHelper.iter ( this.xml.path ( de, "versioning/snapshotVersions/snapshotVersion" ) ) )
                {
                    final MavenInformation info = new MavenInformation ();
                    info.setGroupId ( groupId );
                    info.setArtifactId ( artifactId );
                    info.setVersion ( version );
                    info.setExtension ( this.xml.getElementValue ( node, "extension" ) );
                    info.setClassifier ( this.xml.getElementValue ( node, "classifier" ) );
                    info.setSnapshotVersion ( this.xml.getElementValue ( node, "value" ) );
                    info.setBuildNumber ( buildNumber );

                    if ( info.getClassifier () != null )
                    {
                        classified.add ( info );
                    }
                    else
                    {
                        plain.add ( info );
                    }
                }

                store ( channel, plain, classified );
            }
        }
    }

    private MavenInformation detect ( final String groupId, final String artifactId, final String version, final String name )
    {
        final StringBuilder sb = new StringBuilder ();

        sb.append ( Pattern.quote ( artifactId ) );
        sb.append ( "-" );
        sb.append ( Pattern.quote ( version ) );
        sb.append ( "(|-(?<cl>[^\\.]+))" );
        sb.append ( "(|\\.(?<ext>.*+))" );

        final Pattern p = Pattern.compile ( sb.toString () );

        final Matcher m = p.matcher ( name );
        if ( m.matches () )
        {
            final String extension = m.group ( "ext" );
            final String classifier = m.group ( "cl" );
            final MavenInformation result = new MavenInformation ();

            result.setGroupId ( groupId );
            result.setArtifactId ( artifactId );
            result.setVersion ( version );
            result.setClassifier ( classifier );
            result.setExtension ( extension );

            return result;
        }
        return null;
    }

    private void store ( final Channel channel, final List<MavenInformation> plain, final List<MavenInformation> classified ) throws Exception
    {
        for ( final MavenInformation info : plain )
        {
            store ( channel, info, null );
        }
        for ( final MavenInformation info : classified )
        {
            final Artifact parent = getParent ( channel, info.makePlainName () );
            store ( channel, info, parent );
        }
    }

    private Artifact store ( final Channel channel, final MavenInformation info, final Artifact parent ) throws Exception
    {
        return pullFromTemp ( channel, info, ( is ) -> storeArtifact ( channel, info, parent, is ) );
    }

    protected Artifact storeArtifact ( final Channel channel, final MavenInformation info, final Artifact parent, final InputStream is )
    {
        Map<MetaKey, String> md;
        try
        {
            md = MetaKeys.unbind ( info );
        }
        catch ( final Exception e )
        {
            throw new RuntimeException ( e );
        }
        if ( parent != null )
        {
            return parent.attachArtifact ( info.makeName (), is, md );
        }
        else
        {
            return channel.createArtifact ( info.makeName (), is, md );
        }
    }

    private Artifact pullFromTemp ( final Channel channel, final MavenInformation info, final Function<InputStream, Artifact> func ) throws IOException
    {
        final File file = new File ( this.tempRoot.toFile (), String.format ( "%s/%s/%s/%s/%s", channel.getId (), info.getGroupId (), info.getArtifactId (), info.getVersion (), info.makeName () ) );

        logger.debug ( "Pulling in: {}", file );

        if ( !file.exists () )
        {
            return null;
        }

        logger.debug ( "File exists" );

        try ( InputStream is = new BufferedInputStream ( new FileInputStream ( file ) ) )
        {
            return func.apply ( is );
        }
        finally
        {
            // clean up if possible
            file.delete ();
            cleanup ( file );
        }
    }

    protected void cleanup ( final File file )
    {
        logger.debug ( "Cleanup: {}", file );

        File current = file.getParentFile ();
        final File tmp = this.tempRoot.toFile ();
        while ( !current.equals ( tmp ) && current.isDirectory () )
        {
            current.delete ();
            current = current.getParentFile ();
        }
    }

    private boolean isMetaData ( final String[] toks, final String artifactName )
    {
        if ( artifactName.equals ( "maven-metadata.xml" ) )
        {
            return true;
        }
        return false;
    }

    private void dumpSkip ( final HttpServletRequest request ) throws IOException
    {
        System.out.println ( "----------------------" );
        ByteStreams.copy ( request.getInputStream (), System.out );
        System.out.println ();
        System.out.println ( "----------------------" );
    }

    private static String join ( final String[] toks, final int start, final int remove )
    {
        final StringBuilder sb = new StringBuilder ();
        for ( int i = start; i < toks.length + remove; i++ )
        {
            final String s = toks[i];
            if ( sb.length () > 0 )
            {
                sb.append ( '.' );
            }
            sb.append ( s );
        }
        return sb.toString ();
    }

    private String getGroupId ( final String[] toks )
    {
        return join ( toks, 2, -3 );
    }

    private String getArtifactId ( final String[] toks )
    {
        return toks[toks.length - 3];
    }

    private String getVersion ( final String[] toks )
    {
        return toks[toks.length - 2];
    }

    private boolean isUpload ( final String[] toks, final String artifactName )
    {
        if ( artifactName.startsWith ( "maven-metadata.xml" ) )
        {
            return false;
        }
        if ( artifactName.endsWith ( ".md5" ) )
        {
            return false;
        }
        if ( artifactName.endsWith ( ".sha1" ) )
        {
            return false;
        }
        return true;
    }

    private boolean authenticate ( final Channel channel, final HttpServletRequest request, final HttpServletResponse response ) throws IOException
    {
        if ( isAuthenticated ( channel, request ) )
        {
            return true;
        }

        BasicAuthentication.request ( response, "channel-" + channel.getId (), "Please authenticate" );

        return false;
    }

    private boolean isAuthenticated ( final Channel channel, final HttpServletRequest request )
    {
        final String[] authToks = parseAuthorization ( request );

        if ( authToks == null )
        {
            return false;
        }

        if ( !authToks[0].equals ( "deploy" ) )
        {
            return false;
        }

        final String deployKey = authToks[1];

        logger.debug ( "Deploy key: '{}'", deployKey );

        for ( final DeployKey key : channel.getAllDeployKeys () )
        {
            if ( key.getKey ().equals ( deployKey ) )
            {
                return true;
            }
        }

        return false;
    }
}
