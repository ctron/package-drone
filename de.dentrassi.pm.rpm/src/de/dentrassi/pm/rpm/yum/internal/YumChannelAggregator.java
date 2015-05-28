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
package de.dentrassi.pm.rpm.yum.internal;

import java.util.HashMap;
import java.util.Map;

import de.dentrassi.pm.aspect.aggregate.AggregationContext;
import de.dentrassi.pm.aspect.aggregate.ChannelAggregator;
import de.dentrassi.pm.aspect.common.spool.ChannelCacheTarget;
import de.dentrassi.pm.common.ArtifactInformation;
import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.rpm.Constants;
import de.dentrassi.pm.rpm.RpmInformation;
import de.dentrassi.pm.rpm.yum.RepositoryCreator;

public class YumChannelAggregator implements ChannelAggregator
{

    @Override
    public Map<String, String> aggregateMetaData ( final AggregationContext context ) throws Exception
    {
        final RepositoryCreator creator = new RepositoryCreator ( new ChannelCacheTarget ( context ) );

        final Map<String, String> result = new HashMap<> ();

        creator.process ( repoContext -> {
            for ( final ArtifactInformation art : context.getArtifacts () )
            {
                final RpmInformation info = RpmInformation.fromJson ( art.getMetaData ().get ( Constants.KEY_INFO ) );

                if ( info == null )
                {
                    continue;
                }

                final String sha1 = art.getMetaData ().get ( new MetaKey ( "hasher", "sha1" ) );

                repoContext.addPackage ( sha1, art, info );
            }
        } );

        return result;
    }
}
