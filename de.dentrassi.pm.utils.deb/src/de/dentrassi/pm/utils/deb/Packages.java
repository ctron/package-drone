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
import java.io.PrintWriter;
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
                        return convert ( new BinaryPackageControlFile ( inputStream ) );
                    }
                }
            }
        }
        return null;
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
}
