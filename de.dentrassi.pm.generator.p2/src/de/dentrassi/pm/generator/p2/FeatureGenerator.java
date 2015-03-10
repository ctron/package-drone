/*******************************************************************************
 * Copyright (c) 2014, 2015 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package de.dentrassi.pm.generator.p2;

import static de.dentrassi.pm.common.MetaKeys.getString;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.dentrassi.osgi.web.LinkTarget;
import de.dentrassi.pm.common.ArtifactInformation;
import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.common.XmlHelper;
import de.dentrassi.pm.generator.ArtifactGenerator;
import de.dentrassi.pm.generator.GenerationContext;
import de.dentrassi.pm.osgi.bundle.BundleInformation;
import de.dentrassi.pm.storage.StorageAccessor;

public class FeatureGenerator implements ArtifactGenerator
{

    private static final String QUALIFIER_SUFFIX = ".qualifier";

    private static final DateFormat QUALIFIER_DATE_FORMAT = new SimpleDateFormat ( "yyyyMMddHHmm" );

    static
    {
        QUALIFIER_DATE_FORMAT.setTimeZone ( TimeZone.getTimeZone ( "UTC" ) );
    }

    public static final String ID = "p2.feature";

    private final XmlHelper xml;

    public FeatureGenerator ()
    {
        this.xml = new XmlHelper ();
    }

    @Override
    public LinkTarget getAddTarget ()
    {
        return LinkTarget.createFromController ( GeneratorController.class, "createFeature" );
    }

    @Override
    public LinkTarget getEditTarget ( final String artifactId )
    {
        final String url = LinkTarget.createFromController ( GeneratorController.class, "editFeature" ).render ( Collections.singletonMap ( "artifactId", artifactId ) );
        return new LinkTarget ( url );
    }

    @Override
    public void generate ( final GenerationContext context ) throws Exception
    {
        final String id = getString ( context.getArtifactInformation ().getMetaData (), ID, "id" );
        final String version = getString ( context.getArtifactInformation ().getMetaData (), ID, "version" );

        final Path tmp = Files.createTempFile ( "p2-feat-", ".jar" );

        try
        {
            try ( final ZipOutputStream jar = new ZipOutputStream ( new FileOutputStream ( tmp.toFile () ) ) )
            {
                final ZipEntry ze = new ZipEntry ( "feature.xml" );
                jar.putNextEntry ( ze );
                createFeatureXml ( jar, context.getArtifactInformation ().getMetaData (), context.getStorage (), context.getArtifactInformation ().getChannelId () );
            }

            final Map<MetaKey, String> providedMetaData = new HashMap<> ();
            try ( BufferedInputStream is = new BufferedInputStream ( new FileInputStream ( tmp.toFile () ) ) )
            {
                context.createVirtualArtifact ( String.format ( "%s-%s.jar", id, version ), is, providedMetaData );
            }
        }
        finally
        {
            Files.deleteIfExists ( tmp );
        }
    }

    private void createFeatureXml ( final OutputStream out, final Map<MetaKey, String> map, final StorageAccessor storage, final String channelid ) throws Exception
    {
        final String id = getString ( map, ID, "id" );
        final String version = makeVersion ( getString ( map, ID, "version" ) );
        final String label = getString ( map, ID, "label" );

        final String description = getString ( map, ID, "description" );
        final String provider = getString ( map, ID, "provider" );

        final Document doc = this.xml.create ();
        final Element root = doc.createElement ( "feature" );
        doc.appendChild ( root );

        root.setAttribute ( "id", id );
        root.setAttribute ( "version", version );
        root.setAttribute ( "label", label );

        if ( provider != null )
        {
            root.setAttribute ( "provider-name", provider );
        }
        if ( description != null )
        {
            XmlHelper.addElement ( root, "description" ).setTextContent ( description );
        }

        for ( final ArtifactInformation a : storage.getArtifacts ( channelid ) )
        {
            processPlugin ( root, a );
        }

        this.xml.write ( doc, out );
    }

    private String makeVersion ( String version )
    {
        if ( version == null )
        {
            return "0.0.0";
        }

        if ( !version.endsWith ( QUALIFIER_SUFFIX ) )
        {
            return version;
        }

        version = version.substring ( 0, version.length () - QUALIFIER_SUFFIX.length () );

        return version + "." + makeTimestamp ( System.currentTimeMillis () );
    }

    private String makeTimestamp ( final long time )
    {
        return FeatureGenerator.QUALIFIER_DATE_FORMAT.format ( new Date ( time ) );
    }

    private void processPlugin ( final Element root, final ArtifactInformation a )
    {
        if ( !Helper.isBundle ( a.getMetaData () ) )
        {
            return;
        }

        final String id = a.getMetaData ().get ( new MetaKey ( "osgi", "name" ) );
        final String version = a.getMetaData ().get ( new MetaKey ( "osgi", "version" ) );
        if ( id == null || version == null )
        {
            return;
        }

        boolean unpack = false;

        try
        {
            final Gson gson = new GsonBuilder ().create ();
            final BundleInformation bi = gson.fromJson ( a.getMetaData ().get ( new MetaKey ( "osgi", "bundle-information" ) ), BundleInformation.class );
            unpack = "dir".equals ( bi.getEclipseBundleShape () );
        }
        catch ( final Exception e )
        {
        }

        final Element p = root.getOwnerDocument ().createElement ( "plugin" );
        root.appendChild ( p );

        p.setAttribute ( "id", id );
        p.setAttribute ( "version", version );
        p.setAttribute ( "unpack", "" + unpack );
    }

    @Override
    public boolean shouldRegenerate ( final Object event )
    {
        return Helper.shouldRegenerateFeature ( event );
    }

}
