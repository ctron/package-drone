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
package de.dentrassi.pm.npm.aspect;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

import com.google.common.io.ByteStreams;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import de.dentrassi.pm.aspect.ChannelAspect;
import de.dentrassi.pm.aspect.extract.Extractor;

public class NpmExtractor implements Extractor
{

    private final ChannelAspect channelAspect;

    public NpmExtractor ( final ChannelAspect channelAspect )
    {
        this.channelAspect = channelAspect;
    }

    @Override
    public ChannelAspect getAspect ()
    {
        return this.channelAspect;
    }

    @Override
    public void extractMetaData ( final Path file, final Map<String, String> metadata ) throws Exception
    {
        try
        {
            perform ( file, metadata );
        }
        catch ( final Exception e )
        {
        }
    }

    private void perform ( final Path file, final Map<String, String> metadata ) throws IOException
    {
        try ( final GZIPInputStream gis = new GZIPInputStream ( new FileInputStream ( file.toFile () ) );
              final TarArchiveInputStream tis = new TarArchiveInputStream ( gis ) )
        {
            TarArchiveEntry entry;
            while ( ( entry = tis.getNextTarEntry () ) != null )
            {
                if ( entry.getName ().equals ( "package/package.json" ) )
                {
                    final byte[] data = new byte[(int)entry.getSize ()];
                    ByteStreams.read ( tis, data, 0, data.length );

                    final String str = StandardCharsets.UTF_8.decode ( ByteBuffer.wrap ( data ) ).toString ();

                    try
                    {
                        // test parse
                        new JsonParser ().parse ( str );
                        // store
                        metadata.put ( "package.json", str );
                    }
                    catch ( final JsonParseException e )
                    {
                        // ignore
                    }

                    break; // stop parsing the archive
                }
            }

        }
    }
}
