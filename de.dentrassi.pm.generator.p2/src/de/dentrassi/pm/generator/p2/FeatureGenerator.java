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
package de.dentrassi.pm.generator.p2;

import static de.dentrassi.pm.common.MetaKeys.getString;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.dentrassi.osgi.web.LinkTarget;
import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.common.XmlHelper;
import de.dentrassi.pm.generator.ArtifactGenerator;
import de.dentrassi.pm.generator.GenerationContext;
import de.dentrassi.pm.storage.Artifact;
import de.dentrassi.pm.storage.Channel;

public class FeatureGenerator implements ArtifactGenerator
{
    public static final String ID = "p2.feature";

    private final XmlHelper xml;

    public FeatureGenerator ()
    {
        this.xml = new XmlHelper ();
    }

    @Override
    public LinkTarget getAddTarget ()
    {
        return LinkTarget.createFromController ( GeneratorController.class, "create" );
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
                createFeatureXml ( jar, context.getArtifactInformation ().getMetaData (), context.getChannel () );
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

    private void createFeatureXml ( final OutputStream out, final Map<MetaKey, String> map, final Channel channel ) throws Exception
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

        for ( final Artifact a : channel.getArtifacts () )
        {
            processPlugin ( root, a );
        }

        this.xml.write ( doc, out );
    }

    private void processPlugin ( final Element root, final Artifact a )
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

        final Element p = root.getOwnerDocument ().createElement ( "plugin" );
        root.appendChild ( p );

        p.setAttribute ( "id", id );
        p.setAttribute ( "version", version );
        p.setAttribute ( "unpack", "false" );
    }
}
