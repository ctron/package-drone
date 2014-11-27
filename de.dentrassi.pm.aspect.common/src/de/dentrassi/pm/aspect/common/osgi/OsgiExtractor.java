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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Map;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.gson.GsonBuilder;

import de.dentrassi.pm.aspect.ChannelAspect;
import de.dentrassi.pm.aspect.extract.Extractor;
import de.dentrassi.pm.common.XmlHelper;
import de.dentrassi.pm.osgi.BundleInformation;
import de.dentrassi.pm.osgi.BundleInformationParser;

public class OsgiExtractor implements Extractor
{
    public static final String KEY_CLASSIFIER = "classifier";

    public static final String KEY_MANIFEST = "manifest";

    public static final String KEY_VERSION = "version";

    public static final String KEY_NAME = "name";

    public static final String KEY_BUNDLE_INFORMATION = "bundle-information";

    private final ChannelAspect aspect;

    private final XmlHelper xml;

    public OsgiExtractor ( final ChannelAspect aspect )
    {
        this.aspect = aspect;
        this.xml = new XmlHelper ();
    }

    @Override
    public ChannelAspect getAspect ()
    {
        return this.aspect;
    }

    @Override
    public void extractMetaData ( final Path file, final Map<String, String> metadata ) throws Exception
    {
        extractBundleInformation ( file, metadata );
        extractFeatureInformation ( file, metadata );
    }

    private void extractFeatureInformation ( final Path file, final Map<String, String> metadata ) throws Exception
    {
        Document fdoc;
        try ( ZipFile zf = new ZipFile ( file.toFile () ) )
        {
            final ZipEntry ze = zf.getEntry ( "feature.xml" );
            if ( ze == null )
            {
                return;
            }
            try ( InputStream stream = zf.getInputStream ( ze ) )
            {
                fdoc = this.xml.parse ( stream );
            }
        }
        catch ( final ZipException e )
        {
            // silently ignore
            return;
        }

        // process feature content
        final Element root = fdoc.getDocumentElement ();
        if ( !"feature".equals ( root.getNodeName () ) )
        {
            return;
        }

        final String id = root.getAttribute ( "id" );
        final String version = root.getAttribute ( KEY_VERSION );
        if ( id == null || version == null )
        {
            return;
        }

        metadata.put ( KEY_NAME, id );
        metadata.put ( KEY_VERSION, version );
        metadata.put ( KEY_CLASSIFIER, "eclipse.feature" );
    }

    private void extractBundleInformation ( final Path file, final Map<String, String> metadata ) throws Exception
    {
        final BundleInformation bi;
        try ( ZipFile zipFile = new ZipFile ( file.toFile () ) )
        {
            bi = new BundleInformationParser ( zipFile ).parse ();
            if ( bi == null )
            {
                return;
            }
        }
        catch ( final ZipException e )
        {
            return;
        }

        // store main attributes
        metadata.put ( KEY_NAME, bi.getId () );
        metadata.put ( KEY_VERSION, bi.getVersion () );
        metadata.put ( KEY_CLASSIFIER, "bundle" );

        // serialize manifest
        final ByteArrayOutputStream bos = new ByteArrayOutputStream ();
        final Manifest mf = new Manifest ();
        mf.write ( bos );
        bos.close ();
        metadata.put ( KEY_MANIFEST, bos.toString ( "UTF-8" ) );

        // store bundle information
        final GsonBuilder gb = new GsonBuilder ();
        metadata.put ( KEY_BUNDLE_INFORMATION, gb.create ().toJson ( bi ) );
    }

}
