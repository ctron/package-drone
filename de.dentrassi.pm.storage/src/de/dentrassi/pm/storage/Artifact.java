/*******************************************************************************
 * Copyright (c) 2014, 2015 Jens Reimann.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jens Reimann - initial API and implementation
 *******************************************************************************/
package de.dentrassi.pm.storage;

import java.io.InputStream;
import java.util.Comparator;
import java.util.Map;

import de.dentrassi.pm.common.ArtifactInformation;
import de.dentrassi.pm.common.MetaKey;

public interface Artifact extends Comparable<Artifact>
{
    public Channel getChannel ();

    public String getId ();

    public void streamData ( ArtifactReceiver receiver );

    public void applyMetaData ( Map<MetaKey, String> metadata );

    public Artifact getParent ();

    public ArtifactInformation getInformation ();

    public Artifact attachArtifact ( String name, InputStream stream, Map<MetaKey, String> providedMetaData );

    @Override
    default public int compareTo ( final Artifact o )
    {
        if ( o == null )
        {
            return 1;
        }

        return getId ().compareTo ( o.getId () );
    }

    public static Comparator<Artifact> NAME_COMPARATOR = new Comparator<Artifact> () {

        @Override
        public int compare ( final Artifact o1, final Artifact o2 )
        {
            final int result = o1.getInformation ().getName ().compareTo ( o2.getInformation ().getName () );
            if ( result != 0 )
            {
                return result;
            }
            return o1.getId ().compareTo ( o2.getId () );
        }
    };

}
