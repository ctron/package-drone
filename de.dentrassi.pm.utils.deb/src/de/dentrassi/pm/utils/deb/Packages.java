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
package de.dentrassi.pm.utils.deb;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ar.ArArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.vafer.jdeb.debian.BinaryPackageControlFile;
import org.vafer.jdeb.debian.ControlField;
import org.vafer.jdeb.debian.ControlFile;

import de.dentrassi.osgi.utils.Strings;
import de.dentrassi.pm.utils.deb.internal.BinarySectionPackagesFile;
import de.dentrassi.pm.utils.deb.internal.StatusFileEntry;

public class Packages
{
    public static SortedMap<String, String> parseControlFile ( final File packageFile ) throws IOException, ParseException
    {
        try ( final ArArchiveInputStream in = new ArArchiveInputStream ( new FileInputStream ( packageFile ) ) )
        {
            ArchiveEntry ar;
            while ( ( ar = in.getNextEntry () ) != null )
            {
                if ( !ar.getName ().equals ( "control.tar.gz" ) )
                {
                    continue;
                }
                try ( final TarArchiveInputStream inputStream = new TarArchiveInputStream ( new GZIPInputStream ( in ) ) )
                {
                    TarArchiveEntry te;
                    while ( ( te = inputStream.getNextTarEntry () ) != null )
                    {
                        String name = te.getName ();
                        if ( name.startsWith ( "./" ) )
                        {
                            name = name.substring ( 2 );
                        }
                        if ( !name.equals ( "control" ) )
                        {
                            continue;
                        }
                        return parseControlFile ( inputStream );
                    }
                }
            }
        }
        return null;
    }

    public static SortedMap<String, String> parseControlFile ( final InputStream inputStream ) throws IOException, ParseException
    {
        return convert ( new BinaryPackageControlFile ( inputStream ) );
    }

    public static List<SortedMap<String, String>> parseStatusFile ( final InputStream inputStream ) throws IOException, ParseException
    {
        final BufferedInputStream bin = new BufferedInputStream ( inputStream );
        final byte[] d = new byte[1];

        ByteArrayOutputStream bos = new ByteArrayOutputStream ();

        boolean newline = false;
        final List<SortedMap<String, String>> result = new LinkedList<> ();

        while ( bin.read ( d ) > 0 )
        {
            if ( d[0] == '\n' )
            {
                if ( newline )
                {
                    // double newline
                    result.add ( convert ( new StatusFileEntry ( new ByteArrayInputStream ( bos.toByteArray () ) ) ) );

                    bos = new ByteArrayOutputStream ();
                }
                else
                {
                    newline = true;
                    bos.write ( d );
                }
            }
            else
            {
                newline = false;
                bos.write ( d );
            }

        }

        // last entry
        if ( bos.size () > 0 )
        {
            result.add ( convert ( new StatusFileEntry ( new ByteArrayInputStream ( bos.toByteArray () ) ) ) );
        }

        return result;
    }

    private static SortedMap<String, String> convert ( final ControlFile controlFile )
    {
        return new TreeMap<> ( controlFile.getValues () );
    }

    public static void writeBinaryPackageValues ( final PrintWriter writer, final Map<String, String> values )
    {
        final BinarySectionPackagesFile file = new BinarySectionPackagesFile ();
        for ( final Map.Entry<String, String> entry : values.entrySet () )
        {
            file.set ( entry.getKey (), entry.getValue () );
        }

        writer.print ( file.toString () );
    }

    private static final ControlField DESC = new ControlField ( "Description", false, ControlField.Type.MULTILINE );

    private static MessageDigest MD5;

    static
    {
        try
        {
            MD5 = MessageDigest.getInstance ( "MD5" );
        }
        catch ( final NoSuchAlgorithmException e )
        {
            throw new RuntimeException ( e );
        }
    }

    public static String makeDescriptionMd5 ( final String string )
    {
        if ( string == null )
        {
            return null;
        }

        final String result = DESC.format ( string ).substring ( "Description: ".length () );

        final byte[] data = MD5.digest ( result.getBytes ( StandardCharsets.UTF_8 ) );
        return Strings.hex ( data );
    }
}
