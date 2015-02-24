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
package de.dentrassi.pm.sec.service.jpa;

import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dentrassi.osgi.scheduler.ScheduledTask;
import de.dentrassi.pm.common.service.AbstractJpaServiceImpl;
import de.dentrassi.pm.core.CoreService;
import de.dentrassi.pm.sec.jpa.UserEntity;
import de.dentrassi.pm.sec.jpa.UserEntity_;

public class CleanupJob extends AbstractJpaServiceImpl implements ScheduledTask
{
    private final static Logger logger = LoggerFactory.getLogger ( CleanupJob.class );

    private CoreService coreService;

    public void setCoreService ( final CoreService coreService )
    {
        this.coreService = coreService;
    }

    @Override
    public void run ()
    {
        doWithTransactionVoid ( ( em ) -> internalProcess ( em ) );
    }

    protected void internalProcess ( final EntityManager em )
    {
        final CriteriaBuilder cb = em.getCriteriaBuilder ();

        final CriteriaQuery<UserEntity> cq = cb.createQuery ( UserEntity.class );

        final Root<UserEntity> root = cq.from ( UserEntity.class );

        final List<Predicate> predicates = new LinkedList<> ();

        final Timestamp overdue = new Timestamp ( System.currentTimeMillis () - getTimeout () );

        logger.debug ( "Processing all users before {}", overdue );

        predicates.add ( cb.lessThan ( root.get ( UserEntity_.emailTokenDate ), overdue ) );

        cq.where ( cb.or ( predicates.toArray ( new Predicate[predicates.size ()] ) ) );

        final TypedQuery<UserEntity> q = em.createQuery ( cq );

        for ( final UserEntity user : q.getResultList () )
        {
            internalProcessUser ( em, user );
        }
    }

    private long getTimeout ()
    {
        return TimeUnit.HOURS.toMillis ( 1 );
    }

    private void internalProcessUser ( final EntityManager em, final UserEntity user )
    {
        logger.debug ( "Processing user: {}", user.getId () );

        if ( user.isEmailVerified () )
        {
            // just clear the password reset

            logger.info ( "Clear email token" );

            user.setEmailToken ( null );
            user.setEmailTokenDate ( null );
            user.setEmailTokenSalt ( null );
            em.persist ( user );
        }
        else
        {
            // this should still work!

            logger.info ( "Delete user" );

            em.remove ( user );
        }
        em.flush ();
    }
}
