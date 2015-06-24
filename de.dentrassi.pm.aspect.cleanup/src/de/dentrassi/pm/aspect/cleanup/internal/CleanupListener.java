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
package de.dentrassi.pm.aspect.cleanup.internal;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dentrassi.pm.aspect.cleanup.Aggregator;
import de.dentrassi.pm.aspect.cleanup.CleanupConfiguration;
import de.dentrassi.pm.aspect.cleanup.CleanupTester.Action;
import de.dentrassi.pm.aspect.cleanup.ResultEntry;
import de.dentrassi.pm.aspect.cleanup.ResultKey;
import de.dentrassi.pm.aspect.cleanup.Sorter;
import de.dentrassi.pm.aspect.listener.ChannelListener;
import de.dentrassi.pm.aspect.listener.PostAddContext;
import de.dentrassi.pm.common.DetailedArtifactInformation;
import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.common.MetaKeys;

public class CleanupListener implements ChannelListener
{

    private final static Logger logger = LoggerFactory.getLogger ( CleanupListener.class );

    @Override
    public void artifactAdded ( final PostAddContext context ) throws Exception
    {
        final Map<MetaKey, String> metaData = context.getChannelMetaData ();

        final CleanupConfiguration cfg = MetaKeys.bind ( new CleanupConfiguration (), metaData );

        if ( cfg.getNumberOfVersions () <= 0 || cfg.getSorter () == null )
        {
            logger.info ( "Cleanup is unconfigured" );
            return;
        }

        final Map<List<String>, LinkedList<DetailedArtifactInformation>> artifacts = aggregate ( cfg.getAggregator (), cfg.getSorter (), cfg.isOnlyRootArtifacts (), context.getChannelArtifacts () );

        final SortedMap<ResultKey, List<ResultEntry>> result = process ( cfg, artifacts );

        for ( final List<ResultEntry> list : result.values () )
        {
            for ( final ResultEntry entry : list )
            {
                if ( entry.getAction () == Action.DELETE )
                {
                    final String id = entry.getArtifact ().getId ();
                    logger.debug ( "Deleting: {}", id );
                    context.deleteArtifact ( id );
                }
            }
        }
    }

    static SortedMap<ResultKey, List<ResultEntry>> process ( final CleanupConfiguration configuration, final Map<List<String>, LinkedList<DetailedArtifactInformation>> aggregation )
    {
        final int numVersions = configuration.getNumberOfVersions ();

        final SortedMap<ResultKey, List<ResultEntry>> result = new TreeMap<> ();

        for ( final Map.Entry<List<String>, LinkedList<DetailedArtifactInformation>> entry : aggregation.entrySet () )
        {
            final ResultKey key = new ResultKey ( entry.getKey () );

            List<ResultEntry> value = result.get ( key );
            if ( value == null )
            {
                value = new LinkedList<> ();
                result.put ( key, value );
            }

            final int cutOff = entry.getValue ().size () - numVersions;
            int i = 0;
            for ( final DetailedArtifactInformation art : entry.getValue () )
            {
                final Action action = i < cutOff ? Action.DELETE : Action.KEEP;
                value.add ( new ResultEntry ( art, action ) );
                i++;
            }
        }

        return result;
    }

    static Map<List<String>, LinkedList<DetailedArtifactInformation>> aggregate ( final Aggregator aggregator, final Sorter sorter, final boolean rootOnly, final Collection<? extends DetailedArtifactInformation> artifacts )
    {
        final Map<List<String>, LinkedList<DetailedArtifactInformation>> result = new HashMap<> ();

        for ( final DetailedArtifactInformation art : artifacts )
        {
            if ( !art.is ( "deletable" ) )
            {
                continue;
            }

            if ( rootOnly && art.getParentId () != null )
            {
                // ignore non-root artifacts
                continue;
            }

            // make key
            final List<String> key = aggregator.makeKey ( art.getMetaData () );

            // get list
            LinkedList<DetailedArtifactInformation> list = result.get ( key );

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

        final Comparator<DetailedArtifactInformation> comparator = sorter.makeComparator ();
        for ( final LinkedList<DetailedArtifactInformation> list : result.values () )
        {
            Collections.sort ( list, comparator );
        }

        return result;
    }
}
