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
package de.dentrassi.pm.storage.service.jpa.guard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dentrassi.pm.common.service.Guard;
import de.dentrassi.pm.storage.jpa.ArtifactDeleteRequestEntity;
import de.dentrassi.pm.storage.jpa.ArtifactTracker;
import de.dentrassi.pm.storage.jpa.ArtifactTracker.Tracker;
import de.dentrassi.pm.storage.service.jpa.blob.BlobStore;

public class ArtifactGuard implements Guard
{
    private final static Logger logger = LoggerFactory.getLogger ( ArtifactGuard.class );

    private final Supplier<EntityManager> entityManagerSupplier;

    private boolean installed;

    private final BlobStore blobStore;

    public ArtifactGuard ( final Supplier<EntityManager> entityManagerSupplier, final BlobStore blobStore )
    {
        this.entityManagerSupplier = entityManagerSupplier;
        this.blobStore = blobStore;
    }

    // ==>> lifecycle methods

    @Override
    public void before ( final EntityManager entityManager )
    {
        this.installed = ArtifactTracker.startTracking ();
    }

    @Override
    public void beforeCommit ( final Object result, final EntityManager entityManager )
    {
        markDeleted ( entityManager, getTracked ( Tracker::getDeletions ) );
    }

    @Override
    public void afterRollback ()
    {
        logger.debug ( "After rollback" );
        doWithTransaction ( em -> markDeleted ( em, getTracked ( Tracker::getAdditions ) ) );
    }

    @Override
    public void afterAll ()
    {
        if ( this.installed )
        {
            ArtifactTracker.stopTracking ();
        }
        // process deletions from our em
        doWithTransaction ( em -> vacuum ( em ) );
    }

    // ==>> helper methods

    private void vacuum ( final EntityManager entityManager )
    {
        final Set<String> deleted = new HashSet<> ();
        for ( final String id : getMarkedArtifacts ( entityManager ) )
        {
            try
            {
                vacuumArtifact ( id );
                deleted.add ( id );
            }
            catch ( final Exception e )
            {
                logger.warn ( "Failed to process artifact deletion: " + id, e );
            }
        }

        deleteVacuumed ( entityManager, deleted );
    }

    private void vacuumArtifact ( final String id ) throws IOException
    {
        logger.debug ( "Vacuuming artifact: {}", id );
        this.blobStore.vacuumArtifact ( id );
    }

    private void deleteVacuumed ( final EntityManager entityManager, final Set<String> deleted )
    {
        if ( deleted.isEmpty () )
        {
            return;
        }

        logger.debug ( "Deleting markers: {}", deleted );

        final Query q = entityManager.createQuery ( String.format ( "DELETE from %s a where a.artifactId in :IDS", ArtifactDeleteRequestEntity.class.getName () ) );
        q.setParameter ( "IDS", deleted );
        final int rc = q.executeUpdate ();
        logger.debug ( "Mass deleted {} entries", rc );
    }

    private Collection<String> getMarkedArtifacts ( final EntityManager entityManager )
    {
        final TypedQuery<String> q = entityManager.createQuery ( String.format ( "SELECT a.artifactId from %s a", ArtifactDeleteRequestEntity.class.getName () ), String.class );
        return new ArrayList<> ( q.getResultList () );
    }

    private void markDeleted ( final EntityManager entityManager, final Set<String> ids )
    {
        if ( ids.isEmpty () )
        {
            return;
        }

        logger.debug ( "Mark for deletion: {}", ids );

        for ( final String id : ids )
        {
            final ArtifactDeleteRequestEntity adre = new ArtifactDeleteRequestEntity ();
            adre.setArtifactId ( id );

            // if it is already in the database, that is ok
            entityManager.merge ( adre );
        }
        entityManager.flush ();
    }

    private Set<String> getTracked ( final Function<Tracker, Set<String>> source )
    {
        final Tracker tracker = ArtifactTracker.getCurrentTracker ();
        if ( tracker == null )
        {
            return Collections.emptySet ();
        }

        return source.apply ( tracker );
    }

    protected void doWithTransaction ( final Consumer<EntityManager> run )
    {
        final EntityManager em = this.entityManagerSupplier.get ();
        try
        {
            final EntityTransaction tx = em.getTransaction ();
            tx.begin ();

            try
            {
                run.accept ( em );
                em.flush ();
                tx.commit ();
            }
            finally
            {
                if ( tx.isActive () )
                {
                    tx.rollback ();
                }
            }
        }
        finally
        {
            em.close ();
        }
    }

}
