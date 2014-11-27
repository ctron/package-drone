/*******************************************************************************
 * Copyright (c) 2014 Jens Reimann.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jens Reimann - initial API and implementation
 *******************************************************************************/
package de.dentrassi.pm.storage.service;

import java.util.Comparator;
import java.util.Map;
import java.util.SortedMap;

import de.dentrassi.pm.storage.MetaKey;

public interface Artifact extends Comparable<Artifact>
{
    public Channel getChannel ();

    public String getId ();

    public long getSize ();

    public String getName ();

    public void streamData ( ArtifactReceiver receiver );

    public SortedMap<MetaKey, String> getMetaData ();

    public void applyMetaData ( Map<MetaKey, String> metadata );

    public boolean isVirtual ();

    public static Comparator<Artifact> NAME_COMPARATOR = new Comparator<Artifact> () {

        @Override
        public int compare ( final Artifact o1, final Artifact o2 )
        {
            final int result = o1.getName ().compareTo ( o2.getName () );
            if ( result != 0 )
            {
                return result;
            }
            return o1.getId ().compareTo ( o2.getId () );
        }
    };
}
