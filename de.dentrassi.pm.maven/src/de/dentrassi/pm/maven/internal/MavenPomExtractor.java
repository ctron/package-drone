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
package de.dentrassi.pm.maven.internal;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import de.dentrassi.pm.aspect.virtual.Virtualizer;

public class MavenPomExtractor implements Virtualizer
{

    @Override
    public void virtualize ( final Context context )
    {
        try ( ZipInputStream zis = new ZipInputStream ( new FileInputStream ( context.getFile ().toFile () ) ) )
        {
            ZipEntry entry;
            while ( ( entry = zis.getNextEntry () ) != null )
            {
                String name = entry.getName ();
                if ( name.startsWith ( "/" ) )
                {
                    name = name.substring ( 1 );
                }

                if ( name.startsWith ( "META-INF/maven/" ) && name.endsWith ( "/pom.xml" ) )
                {
                    extractPom ( entry, zis, context );
                }
            }
        }
        catch ( final IOException e )
        {
            // silently ignore
        }
    }

    private void extractPom ( final ZipEntry entry, final ZipInputStream zis, final Context context )
    {
        context.createVirtualArtifact ( "pom.xml", zis, null );
    }
}
