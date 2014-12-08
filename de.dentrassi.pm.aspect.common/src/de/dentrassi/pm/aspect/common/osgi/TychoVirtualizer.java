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

import static de.dentrassi.pm.common.XmlHelper.addElement;
import static de.dentrassi.pm.common.XmlHelper.fixSize;

import java.io.ByteArrayInputStream;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

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
    private final XmlHelper xml;

    public TychoVirtualizer ()
    {
        this.xml = new XmlHelper ();
    }

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

        final GsonBuilder gb = new GsonBuilder ();

        final String biString = art.getMetaData ().get ( new MetaKey ( OsgiAspectFactory.ID, OsgiExtractor.KEY_BUNDLE_INFORMATION ) );
        if ( biString != null )
        {
            final Gson gson = gb.create ();
            final BundleInformation bi = gson.fromJson ( biString, BundleInformation.class );

            createBundleP2MetaData ( context, art, bi );
            createBundleP2Artifacts ( context, art, bi );
        }

        final String fiString = art.getMetaData ().get ( new MetaKey ( OsgiAspectFactory.ID, OsgiExtractor.KEY_FEATURE_INFORMATION ) );
        if ( fiString != null )
        {
            final Gson gson = gb.create ();
            final FeatureInformation fi = gson.fromJson ( fiString, FeatureInformation.class );

            createFeatureP2MetaData ( context, art, fi );
            createFeatureP2Artifacts ( context, art, fi );
        }
    }

    private void createP2Artifacts ( final Context context, final String id, final String version, final String type, final ArtifactInformation artifact, final String contentType ) throws Exception
    {
        final Document doc = this.xml.create ();

        final Element artifacts = doc.createElement ( "artifacts" );
        doc.appendChild ( artifacts );

        final Element a = addElement ( artifacts, "artifact" );
        a.setAttribute ( "classifier", type );
        a.setAttribute ( "id", id );
        a.setAttribute ( "version", version );

        final String md5 = artifact.getMetaData ().get ( new MetaKey ( "hasher", "md5" ) );

        final Element props = addElement ( a, "properties" );

        if ( md5 != null )
        {
            addProperty ( props, "download.md5", md5 );
        }

        addProperty ( props, "download.size", "" + artifact.getSize () );
        addProperty ( props, "artifact.size", "" + artifact.getSize () );
        addProperty ( props, "download.contentType", contentType );
        addProperty ( props, "drone.artifact.id", artifact.getId () );

        fixSize ( props );
        fixSize ( artifacts );

        createXmlVirtualArtifact ( context, artifact, doc, "-p2artifacts.xml" );
    }

    private void createFeatureP2Artifacts ( final Context context, final ArtifactInformation artifact, final FeatureInformation fi ) throws Exception
    {
        createP2Artifacts ( context, fi.getId (), fi.getVersion (), "org.eclipse.update.feature", artifact, "application/zip" );
    }

    private void createBundleP2Artifacts ( final Context context, final ArtifactInformation artifact, final BundleInformation bi ) throws Exception
    {
        createP2Artifacts ( context, bi.getId (), bi.getVersion (), "osgi.bundle", artifact, null );
    }

    private void addProperty ( final Element props, final String key, final String value )
    {
        if ( value == null )
        {
            return;
        }

        final Element p = addElement ( props, "property" );
        p.setAttribute ( "name", key );
        p.setAttribute ( "value", value );
    }

    private void createFeatureP2MetaData ( final Context context, final ArtifactInformation art, final FeatureInformation fi ) throws Exception
    {
        final List<InstallableUnit> ius = InstallableUnit.fromFeature ( fi );
        createXmlVirtualArtifact ( context, art, InstallableUnit.toXml ( ius ), "-p2metadata.xml" );
    }

    private void createBundleP2MetaData ( final Context context, final ArtifactInformation art, final BundleInformation bi ) throws Exception
    {
        createXmlVirtualArtifact ( context, art, InstallableUnit.fromBundle ( bi ).toXml (), "-p2metadata.xml" );
    }

    private void createXmlVirtualArtifact ( final Context context, final ArtifactInformation art, final Document doc, final String suffix ) throws Exception
    {
        final XmlHelper xml = new XmlHelper ();
        final byte[] data = xml.toData ( doc );

        String name = art.getName ();
        name = name.replaceFirst ( "\\.jar$", suffix );

        context.createVirtualArtifact ( name, new ByteArrayInputStream ( data ), null );
    }

}
