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
package de.dentrassi.pm.importer.http;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.mail.internet.ContentDisposition;
import javax.mail.internet.ParseException;

import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.dentrassi.osgi.web.LinkTarget;
import de.dentrassi.pm.importer.ImportContext;
import de.dentrassi.pm.importer.Importer;
import de.dentrassi.pm.importer.ImporterDescription;
import de.dentrassi.pm.importer.SimpleImporterDescription;

public class HttpImporter implements Importer
{
    public static final String ID = "http";

    private static final SimpleImporterDescription DESCRIPTION;

    private final Gson gson;

    public HttpImporter ()
    {
        this.gson = new GsonBuilder ().create ();
    }

    static
    {
        DESCRIPTION = new SimpleImporterDescription ();
        DESCRIPTION.setId ( ID );
        DESCRIPTION.setLabel ( "HTTP Importer" );
        DESCRIPTION.setDescription ( "Import artifacts by downloading the provided URL" );
        DESCRIPTION.setStartTarget ( new LinkTarget ( "/import/{token}/http/start" ) );
    }

    @Override
    public ImporterDescription getDescription ()
    {
        return DESCRIPTION;
    }

    @Override
    public void runImport ( final ImportContext context, final String configuration ) throws Exception
    {
        final Configuration cfg = this.gson.fromJson ( configuration, Configuration.class );
        System.out.println ( "Get URL: " + cfg.getUrl () );

        final URL url = new URL ( cfg.getUrl () );

        final Path file = Files.createTempFile ( "import", null );

        final URLConnection con = url.openConnection ();

        String name;

        try ( final InputStream in = con.getInputStream ();
              OutputStream out = new BufferedOutputStream ( new FileOutputStream ( file.toFile () ) ) )
        {
            ByteStreams.copy ( in, out );

            // get the name inside here, since this will properly clean up if something fails

            name = makeName ( cfg, url, con );
            if ( name == null )
            {
                throw new IllegalStateException ( String.format ( "Unable to determine name for %s", cfg.getUrl () ) );
            }
        }
        catch ( final Exception e )
        {
            Files.deleteIfExists ( file );
            throw e;
        }

        context.scheduleImport ( file, name );
    }

    public static String makeName ( final Configuration cfg, final URL url, final URLConnection con )
    {
        String name = cfg.getAlternateName ();

        if ( name == null || name.isEmpty () )
        {
            name = fromContentDisposition ( con.getHeaderField ( "Content-Disposition" ) );
        }
        if ( name == null || name.isEmpty () )
        {
            name = fromPath ( url.getPath () );
        }
        return name;
    }

    private static String fromContentDisposition ( final String field )
    {
        if ( field == null || field.isEmpty () )
        {
            return null;
        }

        try
        {
            final ContentDisposition cd = new ContentDisposition ( field );
            return cd.getParameter ( "filename" );
        }
        catch ( final ParseException e )
        {
            return null;
        }
    }

    private static String fromPath ( final String path )
    {
        if ( path == null )
        {
            return null;
        }

        final String[] segs = path.split ( "/" );

        if ( segs.length == 0 )
        {
            return null;
        }

        return segs[segs.length - 1];
    }
}