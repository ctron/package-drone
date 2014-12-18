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
package de.dentrassi.pm.aspect.common.eclipse;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import de.dentrassi.pm.aspect.virtual.Virtualizer;
import de.dentrassi.pm.common.ArtifactInformation;
import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.osgi.bundle.BundleInformation;

public class MavenSourceBundleVirtualizer implements Virtualizer
{

    @Override
    public void virtualize ( final Context context )
    {
        final ArtifactInformation ai = context.getArtifactInformation ();
        if ( !ai.getName ().endsWith ( "-sources.jar" ) )
        {
            return;
        }

        final BundleInformation bi = findBundleInformation ();
        if ( bi == null )
        {
            return;
        }

        try
        {
            createSourceBundle ( context, bi );
        }
        catch ( final IOException e )
        {

        }
    }

    protected void createSourceBundle ( final Context context, final BundleInformation bi ) throws IOException, FileNotFoundException
    {
        final Map<MetaKey, String> providedMetaData = new HashMap<> ();

        final Path tmp = Files.createTempFile ( "src-", null );
        try
        {
            final String name = String.format ( "%s.source_%s.jar", bi.getName (), bi.getVersion () );

            // createSourceBundle ( tmp, context );
            try ( BufferedInputStream in = new BufferedInputStream ( new FileInputStream ( tmp.toFile () ) ) )
            {
                context.createVirtualArtifact ( "", in, providedMetaData );
            }
        }
        finally
        {
            Files.deleteIfExists ( tmp );
        }
    }

    private BundleInformation findBundleInformation ()
    {
        // TODO Auto-generated method stub
        return null;
    }
}
