/*******************************************************************************
 * Copyright (c) 2015 Jens Reimann.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jens Reimann - initial API and implementation
 *******************************************************************************/
package de.dentrassi.pm.aspect.cleanup.internal;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.scada.utils.str.StringHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dentrassi.pm.aspect.aggregate.AggregationContext;
import de.dentrassi.pm.aspect.aggregate.ChannelAggregator;
import de.dentrassi.pm.aspect.cleanup.Aggregator;
import de.dentrassi.pm.aspect.cleanup.CleanupConfiguration;
import de.dentrassi.pm.aspect.cleanup.Sorter;
import de.dentrassi.pm.common.ArtifactInformation;
import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.common.MetaKeys;

public class CleanupAggregator implements ChannelAggregator
{

    private final static Logger logger = LoggerFactory.getLogger ( CleanupAggregator.class );

    @Override
    public String getId ()
    {
        return CleanupAspect.ID;
    }

    @Override
    public Map<String, String> aggregateMetaData ( final AggregationContext context ) throws Exception
    {
        final Map<MetaKey, String> metaData = context.getChannelMetaData ();

        final CleanupConfiguration cfg = MetaKeys.bind ( new CleanupConfiguration (), metaData );

        if ( cfg.getNumberOfVersions () <= 0 || cfg.getSorter () == null )
        {
            logger.info ( "Cleanup is unconfigured" );
            return null;
        }

        final Map<List<String>, LinkedList<ArtifactInformation>> artifacts = aggregate ( cfg.getAggregator (), cfg.getSorter (), context.getArtifacts () );

        final StringWriter sw = new StringWriter ();
        final PrintWriter pw = new PrintWriter ( sw );
        pw.println ();

        for ( final Map.Entry<List<String>, LinkedList<ArtifactInformation>> entry : artifacts.entrySet () )
        {
            pw.format ( "%s:%n", StringHelper.join ( entry.getKey (), "|" ) );
            final LinkedList<ArtifactInformation> list = entry.getValue ();

            int c = cfg.getNumberOfVersions ();
            while ( !list.isEmpty () && c >= 0 )
            {
                final ArtifactInformation art = list.getLast ();
                pw.format ( "\tKeep %s - %s%n", art.getId (), art.getName () );
                list.removeLast ();
                c--;
            }

            for ( final ArtifactInformation art : list )
            {
                pw.format ( "\tDelete %s - %s%n", art.getId (), art.getName () );
            }
        }
        pw.flush ();
        logger.info ( "Result: {}", sw.toString () );

        return null;
    }

    static Map<List<String>, LinkedList<ArtifactInformation>> aggregate ( final Aggregator aggregator, final Sorter sorter, final Collection<ArtifactInformation> artifacts )
    {
        final Map<List<String>, LinkedList<ArtifactInformation>> result = new HashMap<> ();

        for ( final ArtifactInformation art : artifacts )
        {
            if ( !art.is ( "deletable" ) )
            {
                continue;
            }

            // make key
            final List<String> key = aggregator.makeKey ( art.getMetaData () );

            // get list
            LinkedList<ArtifactInformation> list = result.get ( key );

            // .. or create and put
            if ( list == null )
            {
                list = new LinkedList<> ();
                result.put ( key, list );
            }

            // add entry
            list.add ( art );
        }

        // sort by fields

        final Comparator<ArtifactInformation> comparator = sorter.makeComparator ();
        for ( final LinkedList<ArtifactInformation> list : result.values () )
        {
            Collections.sort ( list, comparator );
        }

        return result;
    }
}
