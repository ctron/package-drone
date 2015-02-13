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
package de.dentrassi.pm.utils.deb;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
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

import de.dentrassi.osgi.utils.Strings;
import de.dentrassi.pm.utils.deb.internal.BinarySectionPackagesFile;

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
                        if ( !te.getName ().equals ( "./control" ) )
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

    private static SortedMap<String, String> convert ( final BinaryPackageControlFile controlFile )
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
