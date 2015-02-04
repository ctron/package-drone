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
package de.dentrassi.pm.maven;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.google.common.io.ByteStreams;

import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.common.XmlHelper;
import de.dentrassi.pm.storage.Artifact;
import de.dentrassi.pm.storage.Channel;
import de.dentrassi.pm.storage.DeployKey;
import de.dentrassi.pm.storage.service.StorageService;

public class MavenServlet extends HttpServlet
{
    private final static Logger logger = LoggerFactory.getLogger ( MavenServlet.class );

    private static final long serialVersionUID = 1L;

    private ServiceTracker<StorageService, StorageService> tracker;

    private XmlHelper xml;

    @Override
    public void init () throws ServletException
    {
        super.init ();

        this.xml = new XmlHelper ();

        final BundleContext context = FrameworkUtil.getBundle ( MavenServlet.class ).getBundleContext ();
        this.tracker = new ServiceTracker<> ( context, StorageService.class, null );
        this.tracker.open ();
    }

    @Override
    public void destroy ()
    {
        this.tracker.close ();
        super.destroy ();
    }

    @Override
    protected void doGet ( final HttpServletRequest req, final HttpServletResponse resp ) throws ServletException, IOException
    {
        if ( "/".equals ( req.getPathInfo () ) )
        {
            resp.getWriter ().write ( "Package Drone Maven 2 Repository Adapter" );
            resp.setStatus ( HttpServletResponse.SC_OK );
            return;
        }
        resp.setStatus ( HttpServletResponse.SC_NOT_FOUND );
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

            final String[] toks = request.getPathInfo ().split ( "/" );
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

            if ( !authenicate ( channel, request, response ) )
            {
                return;
            }

            if ( isUpload ( toks, artifactName ) )
            {
                final Map<MetaKey, String> metadata = new HashMap<> ();

                metadata.put ( new MetaKey ( "mvn", "groupId" ), getGroupId ( toks ) );
                metadata.put ( new MetaKey ( "mvn", "artifactId" ), getArtifactId ( toks ) );
                metadata.put ( new MetaKey ( "mvn", "version" ), getVersionId ( toks ) );

                channel.createArtifact ( artifactName, request.getInputStream (), metadata );
            }
            else if ( isMetaData ( toks, artifactName ) )
            {
                processMetaData ( channel, toks, request );
            }
            else
            {
                dumpSkip ( request );
            }
            response.setStatus ( HttpServletResponse.SC_OK );
        }
        catch ( final Exception e )
        {
            throw new ServletException ( e );
        }
    }

    private boolean authenicate ( final Channel channel, final HttpServletRequest request, final HttpServletResponse response )
    {
        if ( isAuthenticated ( channel, request ) )
        {
            return true;
        }

        response.setStatus ( HttpServletResponse.SC_UNAUTHORIZED );
        response.setHeader ( "WWW-Authenticate", "Basic realm=\"channel-" + channel.getId () + "\"" );

        return false;
    }

    private boolean isAuthenticated ( final Channel channel, final HttpServletRequest request )
    {
        final String auth = request.getHeader ( "Authorization" );
        logger.debug ( "Auth header: {}", auth );

        if ( auth == null || auth.isEmpty () )
        {
            return false;
        }

        final String[] toks = auth.split ( "\\s" );
        if ( toks.length < 2 )
        {
            return false;
        }

        if ( !"Basic".equalsIgnoreCase ( toks[0] ) )
        {
            return false;
        }

        final byte[] authData = Base64.getDecoder ().decode ( toks[1] );
        String authStr = StandardCharsets.ISO_8859_1.decode ( ByteBuffer.wrap ( authData ) ).toString ();

        logger.debug ( "Auth String: {}", authStr );

        if ( authStr.startsWith ( ":" ) )
        {
            authStr = authStr.substring ( 1 );
        }
        if ( authStr.endsWith ( ":" ) )
        {
            authStr = authStr.substring ( 0, authStr.length () - 1 );
        }

        logger.debug ( "Auth String (cleaned): {}", authStr );

        for ( final DeployKey key : channel.getAllDeployKeys () )
        {
            if ( key.getKey ().equals ( authStr ) )
            {
                return true;
            }
        }

        return false;
    }

    private void processMetaData ( final Channel channel, final String[] toks, final HttpServletRequest request ) throws Exception
    {
        final String groupId = join ( toks, 2, -3 );
        final String artifactId = toks[toks.length - 3];
        final String version = toks[toks.length - 2];

        System.out.format ( "Process metadata: %s - %s - %s%n", groupId, artifactId, version );

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

        final String groupIdXml = this.xml.getElementValue ( de, "groupId" );
        final String artifactIdXml = this.xml.getElementValue ( de, "artifactId" );
        final String versionXml = this.xml.getElementValue ( de, "version" );

        System.out.format ( "%s - %s - %s%n", groupIdXml, artifactIdXml, versionXml );

        final String snapshotTimestamp = this.xml.getElementValue ( de, "versioning/snapshot/timestamp" );
        final String snapshotBuildNumber = this.xml.getElementValue ( de, "versioning/snapshot/buildNumber" );

        if ( snapshotTimestamp != null && snapshotBuildNumber != null )
        {
            // snapshot version
            System.out.format ( "\t%s %s%n", snapshotTimestamp, snapshotBuildNumber );

            for ( final Node node : XmlHelper.iter ( this.xml.path ( de, "versioning/snapshotVersions/snapshotVersion" ) ) )
            {
                final String extension = this.xml.getElementValue ( node, "extension" );
                final String value = this.xml.getElementValue ( node, "value" );
                final String classifier = this.xml.getElementValue ( node, "classifier" );
                System.out.format ( "\t\t%s %s %s%n", extension, value, classifier );

                final Map<MetaKey, String> md = new HashMap<> ();
                if ( classifier != null )
                {
                    md.put ( new MetaKey ( "mvn", "classifier" ), classifier );
                }
                if ( extension != null )
                {
                    md.put ( new MetaKey ( "mvn", "extension" ), extension );
                }
                md.put ( new MetaKey ( "mvn", "snapshotVersion" ), value );

                final Collection<Artifact> artifacts = channel.findByName ( String.format ( "%s-%s%s.%s", artifactId, value, classifier != null ? "-" + classifier : "", extension ) );
                for ( final Artifact artifact : artifacts )
                {
                    final Map<MetaKey, String> amd = artifact.getInformation ().getMetaData ();

                    // check for group id
                    if ( !isSameGroupId ( amd, groupIdXml ) )
                    {
                        continue;
                    }

                    artifact.applyMetaData ( md );
                }
            }
        }
    }

    private boolean isSameGroupId ( final Map<MetaKey, String> metadata, final String groupId )
    {
        final String gid = metadata.get ( new MetaKey ( "mvn", "groupId" ) );
        return groupId.equals ( gid );
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

    private String getVersionId ( final String[] toks )
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
}
