/*******************************************************************************
 * Copyright (c) 2014 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package de.dentrassi.pm.osgi;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class ParserHelper
{
    private ParserHelper ()
    {
    }

    public static Map<String, Properties> loadLocalization ( final ZipFile file, final String loc ) throws IOException
    {
        final Map<String, Properties> locs = new HashMap<> ();

        final Pattern pattern = Pattern.compile ( Pattern.quote ( loc ) + "(|_[a-z]{2}-[A-Z]{2})\\.properties" );

        final Enumeration<? extends ZipEntry> en = file.entries ();
        while ( en.hasMoreElements () )
        {
            final ZipEntry ze = en.nextElement ();
            final Matcher m = pattern.matcher ( ze.getName () );
            if ( m.matches () )
            {
                final String locale = makeLocale ( m.group ( 1 ) );
                final Properties properties = loadProperties ( file, ze );
                locs.put ( locale, properties );
            }
        }
        return locs;
    }

    private static Properties loadProperties ( final ZipFile file, final ZipEntry ze ) throws IOException
    {
        final Properties p = new Properties ();
        p.load ( file.getInputStream ( ze ) );
        return p;
    }

    private static String makeLocale ( final String localeString )
    {
        if ( localeString.isEmpty () )
        {
            return "df_LT";
        }
        else
        {
            return localeString;
        }
    }
}
