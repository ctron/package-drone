/*******************************************************************************
 * Copyright (c) 2015 Jens Reimann.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jens Reimann - initial API and implementation
 *******************************************************************************/
package de.dentrassi.pm.maven.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scada.utils.str.ExtendedPropertiesReplacer;
import org.eclipse.scada.utils.str.StringReplacer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.html.HtmlEscapers;
import com.google.common.io.CharStreams;

import de.dentrassi.pm.maven.ChannelData;
import de.dentrassi.pm.maven.ChannelData.ArtifactNode;
import de.dentrassi.pm.maven.ChannelData.DataNode;
import de.dentrassi.pm.maven.ChannelData.DirectoryNode;
import de.dentrassi.pm.maven.ChannelData.Node;
import de.dentrassi.pm.storage.service.StorageService;
import de.dentrassi.pm.storage.service.util.DownloadHelper;

public class MavenHandler
{
    private final static Logger logger = LoggerFactory.getLogger ( MavenHandler.class );

    private final ChannelData channelData;

    private final StorageService service;

    public MavenHandler ( final StorageService service, final ChannelData channelData )
    {
        this.service = service;
        this.channelData = channelData;
    }

    public void handle ( final String path, final HttpServletRequest request, final HttpServletResponse response ) throws IOException
    {
        final LinkedList<String> segs = path != null ? new LinkedList<> ( Arrays.asList ( path.split ( "/" ) ) ) : new LinkedList<> ();

        final Node node = this.channelData.findNode ( segs );
        if ( node == null )
        {
            response.getWriter ().format ( "Unable to find: '%s'", path == null ? "" : path );
            response.setStatus ( HttpServletResponse.SC_NOT_FOUND );
            return;
        }

        logger.debug ( "{} : Node - {}", path, node );

        if ( node instanceof DirectoryNode )
        {
            renderDir ( response, (DirectoryNode)node, path );
        }
        else if ( node instanceof DataNode )
        {
            response.getOutputStream ().write ( ( (DataNode)node ).getData () );
        }
        else if ( node instanceof ArtifactNode )
        {
            download ( response, (ArtifactNode)node );
        }
        // FIXME: right here right now
    }

    private void download ( final HttpServletResponse response, final ArtifactNode node )
    {
        DownloadHelper.streamArtifact ( response, this.service, node.getArtifactId (), null, false );
    }

    private static class DirRenderer
    {
        private final DirectoryNode dir;

        public DirRenderer ( final DirectoryNode dir )
        {
            this.dir = dir;
        }

        @Override
        public String toString ()
        {
            final StringWriter sw = new StringWriter ();
            final PrintWriter pw = new PrintWriter ( sw );

            render ( pw );

            pw.close ();
            return sw.toString ();
        }

        private void render ( final PrintWriter pw )
        {
            pw.write ( "<ul>\n" );
            pw.write ( "<li><a href=\"..\">..</a></li>" );
            for ( final Map.Entry<String, Node> entry : this.dir.getNodes ().entrySet () )
            {
                final String esc = HtmlEscapers.htmlEscaper ().escape ( entry.getKey () );
                pw.write ( "<li><a href=\"" );
                pw.write ( esc );
                pw.write ( "\">" );
                pw.write ( esc );
                pw.write ( "</a></li>\n" );
            }
            pw.write ( "</ul>\n" );
        }
    }

    private void renderDir ( final HttpServletResponse response, final DirectoryNode dir, final String path ) throws IOException
    {
        final PrintWriter w = response.getWriter ();

        final Map<String, Object> model = new HashMap<> ();
        model.put ( "path", path );
        model.put ( "dir", new DirRenderer ( dir ) );
        w.write ( StringReplacer.replace ( loadResource ( "content/index.html" ), new ExtendedPropertiesReplacer ( model ), StringReplacer.DEFAULT_PATTERN, true ) );
    }

    private String loadResource ( final String name ) throws IOException
    {
        try ( InputStream is = MavenHandler.class.getResourceAsStream ( name );
              Reader r = new InputStreamReader ( is, StandardCharsets.UTF_8 ) )
        {
            return CharStreams.toString ( r );
        }
    }

}
