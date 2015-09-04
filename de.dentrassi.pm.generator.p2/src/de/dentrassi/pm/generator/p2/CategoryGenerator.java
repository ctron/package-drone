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
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Element;

import de.dentrassi.osgi.web.LinkTarget;
import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.common.XmlHelper;
import de.dentrassi.pm.generator.ArtifactGenerator;
import de.dentrassi.pm.generator.GenerationContext;
import de.dentrassi.pm.storage.channel.ArtifactInformation;
import de.dentrassi.pm.storage.channel.ChannelArtifactInformation;

public class CategoryGenerator implements ArtifactGenerator
{
    public static final String ID = "p2.category";

    @Override
    public LinkTarget getAddTarget ()
    {
        return LinkTarget.createFromController ( GeneratorController.class, "createCategory" );
    }

    @Override
    public LinkTarget getEditTarget ( final ChannelArtifactInformation artifact )
    {
        final Map<String, String> model = new HashMap<> ( 2 );

        model.put ( "channelId", artifact.getChannelId ().getId () );
        model.put ( "artifactId", artifact.getId () );

        final String url = LinkTarget.createFromController ( GeneratorController.class, "editCategory" ).render ( model );
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
                createMetaDataXml ( out, context.getArtifactInformation ().getMetaData (), context );
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

    private void createMetaDataXml ( final OutputStream out, final Map<MetaKey, String> map, final GenerationContext context ) throws Exception
    {
        final String id = getString ( map, ID, "id" );
        final String name = getString ( map, ID, "name" );
        final String description = getString ( map, ID, "description" );

        final String version = getString ( map, ID, "version", Helper.makeDefaultVersion () );

        Helper.createFragmentFile ( out, ( units ) -> {

            Helper.createCategoryUnit ( units, id, name, description, version, ( unit ) -> {

                final Set<String> ctx = new HashSet<> ();

                final Element reqs = XmlHelper.addElement ( unit, "requires" );
                for ( final ArtifactInformation a : context.getChannelArtifacts () )
                {
                    if ( Helper.isFeature ( a.getMetaData () ) )
                    {
                        Helper.addFeatureRequirement ( ctx, reqs, a );
                    }
                }
                XmlHelper.fixSize ( reqs );
            } );

        } );
    }

    @Override
    public boolean shouldRegenerate ( final Object event )
    {
        return Helper.shouldRegenerateCategory ( event );
    }

}
