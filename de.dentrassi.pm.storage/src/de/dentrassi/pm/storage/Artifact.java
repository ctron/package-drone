/*******************************************************************************
 * Copyright (c) 2014, 2015 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package de.dentrassi.pm.storage;

import java.io.InputStream;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import de.dentrassi.pm.common.ArtifactInformation;
import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.common.utils.ThrowingConsumer;

/**
 * A handle to an artifact
 * <p>
 * This class represents handle to an artifact in a storage service. It can be
 * held for a longer period of time in order to operate on this artifact.
 * However the artifact might get deleted or modified in the meantime.
 * </p>
 */
public interface Artifact extends Comparable<Artifact>
{
    /**
     * Get a handle to the channel this artifact is stored in
     *
     * @return the channel handle, will never return <code>null</code>
     */
    public Channel getChannel ();

    /**
     * Get the id of the artifact
     *
     * @return the id of this artifact, will never return <code>null</code>
     */
    public String getId ();

    // -- validation

    /**
     * Get the list of validation messages for this artifact
     *
     * @return the list of validation messages, never returns <code>null</code>
     */
    public List<ValidationMessage> getValidationMessages ();

    // -- data

    public boolean streamData ( ArtifactReceiver receiver );

    /**
     * Stream the artifact data
     *
     * @param receiver
     *            the receiver of the InputStream providing the artifact data
     * @return returns <code>true</code> if the artifact was found and streamed,
     *         <code>false</code> otherwise
     */
    public boolean streamData ( ThrowingConsumer<InputStream> receiver );

    public void applyMetaData ( Map<MetaKey, String> metadata );

    /**
     * Get the parent of this artifact
     *
     * @return a handle to the parent (container) artifact, or <code>null</code>
     *         if the artifact is not contained by another artifact
     */
    public Artifact getParent ();

    /**
     * Get detail information about this artifact
     *
     * @return the detail information, never <code>null</code>
     */
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

    public static Comparator<Artifact> CREATION_TIMESTAMP_COMPARATOR = new Comparator<Artifact> () {

        @Override
        public int compare ( final Artifact o1, final Artifact o2 )
        {
            return o1.getInformation ().getCreationTimestamp ().compareTo ( o2.getInformation ().getCreationTimestamp () );
        }
    };

}
