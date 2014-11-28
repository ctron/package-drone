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
package de.dentrassi.pm.aspect.common.osgi;

import java.io.ByteArrayInputStream;
import java.util.List;

import org.w3c.dom.Document;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.dentrassi.pm.aspect.virtual.Virtualizer;
import de.dentrassi.pm.common.XmlHelper;
import de.dentrassi.pm.osgi.bundle.BundleInformation;
import de.dentrassi.pm.osgi.feature.FeatureInformation;
import de.dentrassi.pm.storage.ArtifactInformation;
import de.dentrassi.pm.storage.MetaKey;

public class TychoVirtualizer implements Virtualizer
{
    @Override
    public void virtualize ( final Context context )
    {
        try
        {
            processBundle ( context );
        }
        catch ( final Exception e )
        {
            throw new RuntimeException ( e );
        }
    }

    private void processBundle ( final Context context ) throws Exception
    {
        final ArtifactInformation art = context.getArtifactInformation ();

        final GsonBuilder gb = new GsonBuilder ();

        final String biString = art.getMetaData ().get ( new MetaKey ( OsgiAspectFactory.ID, OsgiExtractor.KEY_BUNDLE_INFORMATION ) );
        if ( biString != null )
        {
            final Gson gson = gb.create ();
            final BundleInformation bi = gson.fromJson ( biString, BundleInformation.class );

            final InstallableUnit iu = InstallableUnit.fromBundle ( bi );
            final Document doc = iu.toXml ();
            final XmlHelper xml = new XmlHelper ();
            final byte[] data = xml.toData ( doc );

            String name = art.getName ();
            name = name.replaceFirst ( "\\.jar$", "-p2metadata.xml" );

            context.createVirtualArtifact ( name, new ByteArrayInputStream ( data ) );
        }

        final String fiString = art.getMetaData ().get ( new MetaKey ( OsgiAspectFactory.ID, OsgiExtractor.KEY_FEATURE_INFORMATION ) );
        if ( fiString != null )
        {
            final Gson gson = gb.create ();
            final FeatureInformation fi = gson.fromJson ( fiString, FeatureInformation.class );

            final List<InstallableUnit> ius = InstallableUnit.fromFeature ( fi );
            final Document doc = InstallableUnit.toXml ( ius );
            final XmlHelper xml = new XmlHelper ();
            final byte[] data = xml.toData ( doc );

            String name = art.getName ();
            name = name.replaceFirst ( "\\.jar$", "-p2metadata.xml" );

            context.createVirtualArtifact ( name, new ByteArrayInputStream ( data ) );
        }
    }
}
