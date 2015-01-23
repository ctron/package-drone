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
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.dentrassi.osgi.web.LinkTarget;
import de.dentrassi.pm.common.ArtifactInformation;
import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.common.XmlHelper;
import de.dentrassi.pm.common.event.AddedEvent;
import de.dentrassi.pm.common.event.RemovedEvent;
import de.dentrassi.pm.generator.ArtifactGenerator;
import de.dentrassi.pm.generator.GenerationContext;
import de.dentrassi.pm.storage.StorageAccessor;

public class CategoryGenerator implements ArtifactGenerator
{
    private final static Logger logger = LoggerFactory.getLogger ( CategoryGenerator.class );

    public static final String ID = "p2.category";

    private final XmlHelper xml;

    public CategoryGenerator ()
    {
        this.xml = new XmlHelper ();
    }

    @Override
    public LinkTarget getAddTarget ()
    {
        return LinkTarget.createFromController ( GeneratorController.class, "createCategory" );
    }

    @Override
    public LinkTarget getEditTarget ( final String artifactId )
    {
        final String url = LinkTarget.createFromController ( GeneratorController.class, "editCategory" ).render ( Collections.singletonMap ( "artifactId", artifactId ) );
        return new LinkTarget ( url );
    }

    @Override
    public void generate ( final GenerationContext context ) throws Exception
    {
        final String id = getString ( context.getArtifactInformation ().getMetaData (), ID, "id" );

        final Path tmp = Files.createTempFile ( "p2-cat-", ".xml" );

        try
        {
            try ( final BufferedOutputStream out = new BufferedOutputStream ( new FileOutputStream ( tmp.toFile () ) ) )
            {
                createMetaDataXml ( out, context.getArtifactInformation ().getMetaData (), context.getStorage (), context.getArtifactInformation ().getChannelId () );
            }

            final Map<MetaKey, String> providedMetaData = new HashMap<> ();
            try ( BufferedInputStream is = new BufferedInputStream ( new FileInputStream ( tmp.toFile () ) ) )
            {
                context.createVirtualArtifact ( String.format ( "%s-p2metadata.xml", id ), is, providedMetaData );
            }
        }
        finally
        {
            Files.deleteIfExists ( tmp );
        }
    }

    private void createMetaDataXml ( final OutputStream out, final Map<MetaKey, String> map, final StorageAccessor storage, final String channelid ) throws Exception
    {
        final String id = getString ( map, ID, "id" );
        final String name = getString ( map, ID, "name" );
        final String description = getString ( map, ID, "description" );

        String version = getString ( map, ID, "version" );
        if ( version == null || version.isEmpty () )
        {
            version = String.format ( "0.0.0.0--%x", System.currentTimeMillis () );
        }

        final Document doc = this.xml.create ();
        final Element units = doc.createElement ( "units" );
        doc.appendChild ( units );

        final Element unit = XmlHelper.addElement ( units, "unit" );

        unit.setAttribute ( "id", id );
        unit.setAttribute ( "version", version );

        final Element props = XmlHelper.addElement ( unit, "properties" );
        addProperty ( props, "org.eclipse.equinox.p2.name", name );
        addProperty ( props, "org.eclipse.equinox.p2.description", description );
        addProperty ( props, "org.eclipse.equinox.p2.type.category", "true" );
        XmlHelper.fixSize ( props );

        {
            final Element provs = XmlHelper.addElement ( unit, "provides" );
            final Element p = XmlHelper.addElement ( provs, "provided" );
            p.setAttribute ( "namespace", "org.eclipse.equinox.p2.iu" );
            p.setAttribute ( "name", id );
            p.setAttribute ( "version", version );
            XmlHelper.fixSize ( provs );
        }

        final Element reqs = XmlHelper.addElement ( unit, "requires" );
        for ( final ArtifactInformation a : storage.getArtifacts ( channelid ) )
        {
            processPlugin ( reqs, a );
        }
        XmlHelper.fixSize ( reqs );

        {
            final Element t = XmlHelper.addElement ( unit, "touchpoint" );
            t.setAttribute ( "id", "null" );
            t.setAttribute ( "version", "0.0.0" );
        }

        XmlHelper.fixSize ( units );

        this.xml.write ( doc, out );
    }

    private void processPlugin ( final Element root, final ArtifactInformation a )
    {
        if ( !isFeature ( a.getMetaData () ) )
        {
            return;
        }

        final String id = a.getMetaData ().get ( new MetaKey ( "osgi", "name" ) );
        if ( id == null )
        {
            return;
        }

        final Element p = root.getOwnerDocument ().createElement ( "required" );
        root.appendChild ( p );

        p.setAttribute ( "namespace", "org.eclipse.equinox.p2.iu" );
        p.setAttribute ( "name", id + ".feature.group" );
        p.setAttribute ( "range", "0.0.0" );
    }

    @Override
    public boolean shouldRegenerate ( final Object event )
    {
        logger.debug ( "Check if we need to generate: {}", event );

        boolean result = false;
        if ( event instanceof AddedEvent )
        {
            final AddedEvent context = (AddedEvent)event;
            result = isFeature ( context.getMetaData () );
        }
        else if ( event instanceof RemovedEvent )
        {
            final RemovedEvent context = (RemovedEvent)event;
            result = isFeature ( context.getMetaData () );
        }

        logger.debug ( "Result: {}", result );

        return result;
    }

    private boolean isFeature ( final Map<MetaKey, String> metaData )
    {
        final String classifier = metaData.get ( new MetaKey ( "osgi", "classifier" ) );
        logger.debug ( "Artifact OSGi classifier: {}", classifier );
        return "eclipse.feature".equals ( classifier );
    }

    protected void addProperty ( final Element parent, final String key, final String value )
    {
        if ( value == null )
        {
            return;
        }

        final Element p = XmlHelper.addElement ( parent, "property" );
        p.setAttribute ( "name", key );
        p.setAttribute ( "value", value );
    }

}
