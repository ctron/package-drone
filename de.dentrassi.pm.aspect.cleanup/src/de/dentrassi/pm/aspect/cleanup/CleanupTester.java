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
package de.dentrassi.pm.aspect.cleanup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;

import de.dentrassi.pm.common.ArtifactInformation;

public interface CleanupTester
{

    public static enum Action
    {
        KEEP,
        DELETE;
    }

    public static class ResultKey implements Comparable<ResultKey>
    {
        private final List<String> keys;

        public ResultKey ( final List<String> keys )
        {
            this.keys = Collections.unmodifiableList ( new ArrayList<> ( keys ) );
        }

        public List<String> getKeys ()
        {
            return this.keys;
        }

        @Override
        public int compareTo ( final ResultKey o )
        {
            final int max = Math.min ( keys.size (), o.keys.size () );

            for ( int i = 0; i < max; i++ )
            {
                final String s1 = keys.get ( i );
                final String s2 = o.keys.get ( i );

                if ( s1 == null && s2 == null )
                {
                    continue;
                }
                if ( s1 == null )
                {
                    return -1;
                }
                if ( s2 == null )
                {
                    return 1;
                }

                final int rc = s1.compareTo ( s2 );
                if ( rc != 0 )
                {
                    return rc;
                }
            }

            if ( keys.size () == o.keys.size () )
            {
                // both lists also have equal size
                return 0;
            }

            if ( keys.size () == max )
            {
                // we are shorter, so show us first
                return 1;
            }
            else
            {
                return 1;
            }
        }

        @Override
        public int hashCode ()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ( keys == null ? 0 : keys.hashCode () );
            return result;
        }

        @Override
        public boolean equals ( final Object obj )
        {
            if ( this == obj )
            {
                return true;
            }
            if ( obj == null )
            {
                return false;
            }
            if ( ! ( obj instanceof ResultKey ) )
            {
                return false;
            }
            final ResultKey other = (ResultKey)obj;
            if ( keys == null )
            {
                if ( other.keys != null )
                {
                    return false;
                }
            }
            else if ( !keys.equals ( other.keys ) )
            {
                return false;
            }
            return true;
        }

    }

    public static class ResultEntry
    {
        private final ArtifactInformation artifact;

        private final Action action;

        public ResultEntry ( final ArtifactInformation artifact, final Action action )
        {
            this.artifact = artifact;
            this.action = action;
        }

        public Action getAction ()
        {
            return this.action;
        }

        public ArtifactInformation getArtifact ()
        {
            return this.artifact;
        }
    }

    public SortedMap<ResultKey, List<ResultEntry>> testCleanup ( final Collection<ArtifactInformation> artifacts, CleanupConfiguration configuration );
}
