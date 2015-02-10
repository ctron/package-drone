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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import de.dentrassi.pm.aspect.cleanup.CleanupConfiguration;
import de.dentrassi.pm.aspect.cleanup.CleanupTester;
import de.dentrassi.pm.common.ArtifactInformation;

public class CleanupTesterImpl implements CleanupTester
{

    @Override
    public SortedMap<ResultKey, List<ResultEntry>> testCleanup ( final Collection<ArtifactInformation> artifacts, final CleanupConfiguration configuration )
    {
        final Map<List<String>, LinkedList<ArtifactInformation>> aggr = CleanupAggregator.aggregate ( configuration.getAggregator (), configuration.getSorter (), artifacts );

        final SortedMap<ResultKey, List<ResultEntry>> result = new TreeMap<> ();

        final int numVersions = configuration.getNumberOfVersions ();

        for ( final Map.Entry<List<String>, LinkedList<ArtifactInformation>> entry : aggr.entrySet () )
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
            for ( final ArtifactInformation art : entry.getValue () )
            {
                final Action action = i < cutOff ? Action.DELETE : Action.KEEP;
                value.add ( new ResultEntry ( art, action ) );
                i++;
            }
        }

        return result;
    }

}
