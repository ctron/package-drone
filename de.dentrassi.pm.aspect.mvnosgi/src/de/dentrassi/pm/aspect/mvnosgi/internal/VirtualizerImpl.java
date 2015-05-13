/*******************************************************************************
 * Copyright (c) 2015 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Rathgeb - initial API and implementation
 *******************************************************************************/
package de.dentrassi.pm.aspect.mvnosgi.internal;

import java.io.IOException;
import java.io.InputStream;

import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dentrassi.pm.aspect.common.osgi.OsgiAspectFactory;
import de.dentrassi.pm.aspect.virtual.Virtualizer;
import de.dentrassi.pm.common.ArtifactInformation;
import de.dentrassi.pm.osgi.bundle.BundleInformation;

public class VirtualizerImpl implements Virtualizer
{
    private final static Logger logger = LoggerFactory.getLogger ( VirtualizerImpl.class );

    private final static String GROUP_ID = "groupid";

    @Override
    public void virtualize ( final Context context )
    {
        try
        {
            processVirtualize ( context );
        }
        catch ( final Exception e )
        {
            throw new RuntimeException ( e );
        }
    }

    private void processVirtualize ( final Context context ) throws Exception
    {
        final ArtifactInformation art = context.getArtifactInformation ();

        logger.debug ( "Process virtualize - artifactId: {} / {}", art.getId (), art.getName () );

        final BundleInformation bi = OsgiAspectFactory.fetchBundleInformation ( art.getMetaData () );
        if ( bi != null )
        {
            final Version version = bi.getVersion ();
            final Pom pom = new Pom ( GROUP_ID, bi.getId (), version.toString () );
            createArtifact ( context, pom );
            return;
        }

        //final FeatureInformation fi = OsgiAspectFactory.fetchFeatureInformation ( art.getMetaData () );
    }

    private void createArtifact ( final Context context, final Pom pom ) throws IOException
    {
        try ( final InputStream inputStream = pom.getInputStream () )
        {
            final String name = makeName ( context.getArtifactInformation ().getName () );
            context.createVirtualArtifact ( name, inputStream, null );
        }
    }

    private String makeName ( final String name )
    {
        if ( name == null )
        {
            return Constants.DEFAULT_POM_NAME;
        }

        final int idx = name.lastIndexOf ( '.' );
        if ( idx < 0 || idx >= name.length () )
        {
            return Constants.DEFAULT_POM_NAME;
        }

        return name.substring ( 0, idx ) + Constants.DEFAULT_POM_POSTIFX;
    }

}
