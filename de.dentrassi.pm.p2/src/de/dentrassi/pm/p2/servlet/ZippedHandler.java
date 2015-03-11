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
package de.dentrassi.pm.p2.servlet;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.io.ByteStreams;

import de.dentrassi.pm.aspect.common.osgi.OsgiExtractor;
import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.common.servlet.Handler;
import de.dentrassi.pm.p2.internal.aspect.ChannelStreamer;
import de.dentrassi.pm.storage.Artifact;
import de.dentrassi.pm.storage.Channel;

public class ZippedHandler implements Handler
{
    private final Channel channel;

    private final Set<String> nameCache = new HashSet<> ();

    public ZippedHandler ( final Channel channel )
    {
        this.channel = channel;
    }

    @Override
    public void prepare () throws Exception
    {
    }

    @Override
    public void process ( final HttpServletRequest req, final HttpServletResponse resp ) throws Exception
    {
        this.nameCache.clear ();

        resp.setContentType ( "application/zip" );
        final ZipOutputStream zos = new ZipOutputStream ( resp.getOutputStream () );

        String channelName = this.channel.getName ();
        if ( channelName == null )
        {
            channelName = this.channel.getId ();
        }

        final ChannelStreamer streamer = new ChannelStreamer ( channelName, this.channel.getMetaData (), false, true );

        for ( final Artifact a : this.channel.getArtifacts () )
        {
            streamer.process ( a.getInformation (), ( ai, receiver ) -> a.streamData ( receiver ) );

            final Map<MetaKey, String> md = a.getInformation ().getMetaData ();

            final String classifier = md.get ( new MetaKey ( "osgi", OsgiExtractor.KEY_CLASSIFIER ) );
            final String symbolicName = md.get ( new MetaKey ( "osgi", OsgiExtractor.KEY_NAME ) );
            final String version = md.get ( new MetaKey ( "osgi", OsgiExtractor.KEY_VERSION ) );

            if ( classifier == null || symbolicName == null || version == null )
            {
                continue;
            }

            final String name = String.format ( "%s_%s.jar", symbolicName, version );

            switch ( classifier )
            {
                case "bundle":
                    stream ( zos, a, "plugins/" + name );
                    break;
                case "eclipse.feature":
                    stream ( zos, a, "features/" + name );
                    break;
            }
        }

        streamer.spoolOut ( ( id, name, mimeType, stream ) -> {
            zos.putNextEntry ( new ZipEntry ( name ) );
            stream.accept ( zos );
            zos.closeEntry ();
        } );

        zos.close ();
    }

    private void stream ( final ZipOutputStream zos, final Artifact a, final String name ) throws IOException
    {
        if ( !this.nameCache.add ( name ) )
        {
            // duplicate entry
            return;
        }

        zos.putNextEntry ( new ZipEntry ( name ) );
        a.streamData ( ( ai, stream ) -> ByteStreams.copy ( stream, zos ) );
        zos.closeEntry ();
    }
}
