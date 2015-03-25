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
package de.dentrassi.pm.storage.jpa;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.PostPersist;
import javax.persistence.PostRemove;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArtifactTracker
{
    private final static Logger logger = LoggerFactory.getLogger ( ArtifactTracker.class );

    public static class Tracker
    {
        private final Set<String> additions = new HashSet<> ();

        private final Set<String> deletions = new HashSet<> ();

        private void addAddition ( final String id )
        {
            this.additions.add ( id );
        }

        private void addDeletion ( final String id )
        {
            this.deletions.add ( id );
        }

        public Set<String> getAdditions ()
        {
            return this.additions;
        }

        public Set<String> getDeletions ()
        {
            return this.deletions;
        }
    }

    private static ThreadLocal<Tracker> trackers = new ThreadLocal<> ();

    @PostRemove
    public void trackDelete ( final ArtifactEntity artifact )
    {
        final String id = artifact.getId ();
        logger.debug ( "Track deletion: {}", id );

        final Tracker tracker = trackers.get ();
        if ( tracker != null )
        {
            tracker.addDeletion ( id );
        }
    }

    @PostPersist
    public void trackAdd ( final ArtifactEntity artifact )
    {
        final String id = artifact.getId ();
        logger.debug ( "Track addition: {}", id );

        final Tracker tracker = trackers.get ();
        if ( tracker != null )
        {
            tracker.addAddition ( id );
        }
    }

    public static Tracker getCurrentTracker ()
    {
        return trackers.get ();
    }

    /**
     * Installs tracking for the current thread
     * <p>
     * This call installs artifact tracking to the current thread if the current
     * thread did not already install tracking. In the case tracking was
     * installed the method will return <code>true</code> and has to be removed
     * using a call to {@link #stopTracking()}.
     * </p>
     * <p>
     * If the call does return <code>false</code> then tracking already was
     * installed and {@link #stopTracking()} must not be called for this call.
     * </p>
     * <p>
     * So if there are nested calls, the first call to {@link #startTracking()}
     * will return <code>true</code>, all following, nested, calls will return
     * <code>false</code>. Therefore only the <em>outer</em> call may call
     * {@link #stopTracking()}.
     * </p>
     *
     * @see #withTracking(Runnable)
     * @see #getCurrentTracker()
     * @return <code>true</code> is the call did install tracking,
     *         <code>false</code> otherwise
     */
    public static boolean startTracking ()
    {
        if ( trackers.get () == null )
        {
            trackers.set ( new Tracker () );
            return true;
        }
        return false;
    }

    /**
     * Removes tracking from the current thread
     * <p>
     * Has no effect is tracking was not installed
     * </p>
     */
    public static void stopTracking ()
    {
        trackers.remove ();
    }

    /**
     * Wraps a call with installed tracking
     * <p>
     * This method installs tracking when necessary, calls the provided runnable
     * and uninstalls tracking if it was installed before the call.
     * </p>
     *
     * @param runnable
     *            the runnable to call once tracking was installed
     * @see #getCurrentTracker()
     */
    public static void withTracking ( final Runnable runnable )
    {
        final boolean added = startTracking ();
        try
        {
            runnable.run ();
        }
        finally
        {
            if ( added )
            {
                stopTracking ();
            }
        }
    }

}
