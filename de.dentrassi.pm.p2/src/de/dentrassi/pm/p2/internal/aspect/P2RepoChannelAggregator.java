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
package de.dentrassi.pm.p2.internal.aspect;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.dentrassi.pm.aspect.aggregate.AggregationContext;
import de.dentrassi.pm.aspect.aggregate.ChannelAggregator;
import de.dentrassi.pm.common.ArtifactInformation;
import de.dentrassi.pm.p2.aspect.P2RepositoryAspect;

public class P2RepoChannelAggregator implements ChannelAggregator
{
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat ( "yyyy-MM-dd HH:mm:ss.SSS" );

    @Override
    public String getId ()
    {
        return P2RepositoryAspect.ID;
    }

    @Override
    public Map<String, String> aggregateMetaData ( final AggregationContext context ) throws Exception
    {
        final Map<String, String> result = new HashMap<> ();

        Date lastTimestamp = null;
        for ( final ArtifactInformation ai : context.getArtifacts () )
        {
            final Date cts = ai.getCreationTimestamp ();

            if ( lastTimestamp == null )
            {
                lastTimestamp = cts;
            }
            else if ( lastTimestamp.before ( cts ) )
            {
                lastTimestamp = cts;
            }
        }

        if ( lastTimestamp != null )
        {
            result.put ( "last-change", "" + lastTimestamp.getTime () );
            result.put ( "last-change-string", DATE_FORMAT.format ( lastTimestamp.getTime () ) );
        }

        return result;
    }

}
