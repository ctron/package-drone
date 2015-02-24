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
        final Map<String, String> result = new HashMap<> ();

        final ChannelData cs = new ChannelData ();

        for ( final ArtifactInformation art : context.getArtifacts () )
        {
            final MavenInformation info = makeInfo ( art );
            if ( info != null )
            {
                // add
                cs.add ( info, art );
            }
        }

        result.put ( "channel", cs.toJson () );

        return result;
    }

    private MavenInformation makeInfo ( final ArtifactInformation art )
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
            return info;
        }
        return null;
    }

}
