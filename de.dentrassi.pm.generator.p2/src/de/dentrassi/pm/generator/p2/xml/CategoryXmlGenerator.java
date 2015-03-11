/*******************************************************************************
 * Copyright (c) 2015 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package de.dentrassi.pm.generator.p2.xml;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.dentrassi.osgi.web.LinkTarget;
import de.dentrassi.pm.common.ArtifactInformation;
import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.common.XmlHelper;
import de.dentrassi.pm.generator.ArtifactGenerator;
import de.dentrassi.pm.generator.GenerationContext;
import de.dentrassi.pm.generator.p2.GeneratorController;
import de.dentrassi.pm.generator.p2.Helper;
import de.dentrassi.pm.generator.p2.Type;
import de.dentrassi.pm.generator.p2.xml.CategoryDefinition.Category;

/**
 * Create categories based on an uploaded <code>category.xml</code>
 */
public class CategoryXmlGenerator implements ArtifactGenerator
{
    public static final String ID = "p2.categoryXml";

    private static final MetaKey MK_OSGI_NAME = new MetaKey ( "osgi", "name" );

    private final XmlHelper xml = new XmlHelper ();

    @Override
    public void generate ( final GenerationContext context ) throws Exception
    {
        Document doc;
        try ( BufferedInputStream is = new BufferedInputStream ( new FileInputStream ( context.getFile ().toFile () ) ) )
        {
            doc = this.xml.parse ( is );
        }

        final CategoryDefinition def = CategoryXmlParser.parse ( doc );

        final String baseName = context.getArtifactInformation ().getName ();

        final Map<String, Set<ArtifactInformation>> map = new HashMap<> ();

        for ( final ArtifactInformation ai : context.getStorage ().getArtifacts ( context.getArtifactInformation ().getChannelId () ) )
        {
            if ( Helper.isBundle ( ai.getMetaData () ) )
            {
                final String id = ai.getMetaData ().get ( MK_OSGI_NAME );
                final Set<String> cats = def.getBundles ().get ( id );
                if ( cats != null )
                {
                    for ( final String cat : cats )
                    {
                        addMap ( map, cat, ai );
                    }
                }
            }

            if ( Helper.isFeature ( ai.getMetaData () ) )
            {
                final String id = ai.getMetaData ().get ( MK_OSGI_NAME );
                final Set<String> cats = def.getFeatures ().get ( id );
                if ( cats != null )
                {
                    for ( final String cat : cats )
                    {
                        addMap ( map, cat, ai );
                    }
                }
            }
        }

        Helper.createFile ( context, baseName + "-p2metadata.xml", ( out ) -> {
            Helper.createFragmentFile ( out, ( units ) -> {
                for ( final Category cat : def.getCategories () )
                {
                    Helper.createCategoryUnit ( units, cat.getId (), cat.getLabel (), cat.getDescription (), Helper.makeDefaultVersion (), ( unit ) -> {
                        final Element reqs = XmlHelper.addElement ( unit, "requires" );

                        final Set<String> ctx = new HashSet<> ();

                        final Set<ArtifactInformation> list = map.get ( cat.getId () );
                        if ( list != null )
                        {
                            for ( final ArtifactInformation ai : list )
                            {
                                if ( Helper.isFeature ( ai.getMetaData () ) )
                                {
                                    Helper.addFeatureRequirement ( ctx, reqs, ai );
                                }
                                else if ( Helper.isBundle ( ai.getMetaData () ) )
                                {
                                    Helper.addBundleRequirement ( ctx, reqs, ai );
                                }
                            }
                        }
                        XmlHelper.fixSize ( reqs );
                    } );
                }
            } );
        } );

    }

    private void addMap ( final Map<String, Set<ArtifactInformation>> map, final String cat, final ArtifactInformation ai )
    {
        Set<ArtifactInformation> list = map.get ( cat );
        if ( list == null )
        {
            list = new HashSet<> ();
            map.put ( cat, list );
        }
        list.add ( ai );
    }

    @Override
    public boolean shouldRegenerate ( final Object event )
    {
        return Helper.changeOf ( event, Type.FEATURE, Type.BUNDLE );
    }

    @Override
    public LinkTarget getAddTarget ()
    {
        return LinkTarget.createFromController ( GeneratorController.class, "createCategoryXml" );
    }

}