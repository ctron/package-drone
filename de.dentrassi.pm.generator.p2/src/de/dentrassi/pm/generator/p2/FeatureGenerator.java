/*******************************************************************************
 * Copyright (c) 2014, 2015 Jens Reimann.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jens Reimann - initial API and implementation
 *******************************************************************************/
package de.dentrassi.pm.generator.p2;

import static de.dentrassi.pm.common.MetaKeys.getString;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.dentrassi.osgi.web.LinkTarget;
import de.dentrassi.pm.common.ArtifactInformation;
import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.common.XmlHelper;
import de.dentrassi.pm.common.event.AddedEvent;
import de.dentrassi.pm.common.event.RemovedEvent;
import de.dentrassi.pm.generator.ArtifactGenerator;
import de.dentrassi.pm.generator.GenerationContext;
import de.dentrassi.pm.osgi.bundle.BundleInformation;
import de.dentrassi.pm.storage.StorageAccessor;

public class FeatureGenerator implements ArtifactGenerator
{

    private final static Logger logger = LoggerFactory.getLogger ( FeatureGenerator.class );

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
        final String version = getString ( map, ID, "version" );
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

    private void processPlugin ( final Element root, final ArtifactInformation a )
    {
        final String classifier = a.getMetaData ().get ( new MetaKey ( "osgi", "classifier" ) );
        if ( !"bundle".equals ( classifier ) )
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
        logger.debug ( "Check if we need to generate: {}", event );

        boolean result = false;
        if ( event instanceof AddedEvent )
        {
            final AddedEvent context = (AddedEvent)event;
            result = isBundle ( context.getMetaData () );
        }
        else if ( event instanceof RemovedEvent )
        {
            final RemovedEvent context = (RemovedEvent)event;
            result = isBundle ( context.getMetaData () );
        }

        logger.debug ( "Result: {}", result );

        return result;
    }

    private boolean isBundle ( final Map<MetaKey, String> metaData )
    {
        final String classifier = metaData.get ( new MetaKey ( "osgi", "classifier" ) );
        logger.debug ( "Artifact OSGi classifier: {}", classifier );
        return "bundle".equals ( classifier );
    }

}
