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
package de.dentrassi.pm.maven.internal;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import de.dentrassi.pm.aspect.aggregate.AggregationContext;
import de.dentrassi.pm.aspect.aggregate.ChannelAggregator;
import de.dentrassi.pm.common.ArtifactInformation;
import de.dentrassi.pm.common.MetaKeys;
import de.dentrassi.pm.maven.ChannelData;
import de.dentrassi.pm.maven.MavenInformation;

public class MavenRepositoryChannelAggregator implements ChannelAggregator
{
    @Override
    public String getId ()
    {
        return MavenRepositoryAspectFactory.ID;
    }

    @Override
    public Map<String, String> aggregateMetaData ( final AggregationContext context ) throws Exception
    {
        final Map<String, ArtifactInformation> map = makeMap ( context );

        final Map<String, String> result = new HashMap<> ();

        final ChannelData cs = new ChannelData ();

        for ( final ArtifactInformation art : context.getArtifacts () )
        {
            final MavenInformation info = makeInfo ( art, map );
            if ( info != null )
            {
                // add
                cs.add ( info, art );
            }
        }

        String json = cs.toJson ();
        result.put ( "channel", json );

        json = cs.toString ();
        context.createCacheEntry ( "channel", "channel.json", "application/json", new ByteArrayInputStream ( json.getBytes ( StandardCharsets.UTF_8 ) ) );

        return result;
    }

    private Map<String, ArtifactInformation> makeMap ( final AggregationContext context )
    {
        final Map<String, ArtifactInformation> result = new HashMap<> ();

        for ( final ArtifactInformation art : context.getArtifacts () )
        {
            result.put ( art.getId (), art );
        }

        return result;
    }

    private MavenInformation makeInfo ( final ArtifactInformation art, final Map<String, ArtifactInformation> map )
    {
        final MavenInformation info = new MavenInformation ();

        try
        {
            MetaKeys.bind ( info, art.getMetaData () );
        }
        catch ( final Exception e )
        {
            return null;
        }

        if ( info.getGroupId () != null & info.getArtifactId () != null && info.getVersion () != null )
        {
            // found direct meta data
            return info;
        }

        final ArtifactInformation pomArt = findChildPom ( art, map );
        if ( pomArt != null )
        {
            try
            {
                MetaKeys.bind ( info, pomArt.getMetaData () );
            }
            catch ( final Exception e )
            {
                return null;
            }

            if ( info.getGroupId () != null & info.getArtifactId () != null && info.getVersion () != null )
            {
                // found pom meta data
                final String ext = makeExtension ( art.getName () );
                if ( ext != null )
                {
                    info.setExtension ( ext );
                    return info;
                }
            }
        }

        return null;
    }

    private static String makeExtension ( final String name )
    {
        final int idx = name.lastIndexOf ( '.' );
        if ( idx < 0 )
        {
            return null;
        }
        return name.substring ( idx + 1 );
    }

    private ArtifactInformation findChildPom ( final ArtifactInformation art, final Map<String, ArtifactInformation> map )
    {
        for ( final String childId : art.getChildIds () )
        {
            final ArtifactInformation child = map.get ( childId );
            if ( child == null )
            {
                continue;
            }
            if ( !child.getName ().equals ( "pom.xml" ) )
            {
                continue;
            }

            return child;
        }

        return null;
    }

}
