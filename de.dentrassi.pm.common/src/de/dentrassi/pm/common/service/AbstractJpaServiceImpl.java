/*******************************************************************************
 * Copyright (c) 2014 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package de.dentrassi.pm.common.service;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

/**
 * A base class for creating services based on JPA
 */
public class AbstractJpaServiceImpl
{
    private EntityManagerFactory entityManagerFactory;

    @FunctionalInterface
    public static interface ManagerFunction<R, T>
    {
        public R process ( T input ) throws Exception;
    }

    @FunctionalInterface
    public static interface VoidManagerFunction
    {
        public void processVoid ( EntityManager entityManager ) throws Exception;
    }

    public void setEntityManagerFactory ( final EntityManagerFactory entityManagerFactory )
    {
        this.entityManagerFactory = entityManagerFactory;
    }

    protected <R> R doWithManager ( final ManagerFunction<R, EntityManager> function ) throws Exception
    {
        final EntityManager em = this.entityManagerFactory.createEntityManager ();
        try
        {
            return function.process ( em );
        }
        finally
        {
            em.close ();
        }
    }

    protected void doWithTransactionVoid ( final VoidManagerFunction function )
    {
        doWithTransaction ( em -> {
            function.processVoid ( em );
            return null;
        } );
    }

    protected <R> R doWithTransaction ( final ManagerFunction<R, EntityManager> function )
    {
        try
        {
            return doWithManager ( entityManager -> {

                final EntityTransaction tx = entityManager.getTransaction ();
                tx.begin ();

                try
                {
                    final R result = function.process ( entityManager );
                    tx.commit ();
                    return result;
                }
                catch ( final Exception e )
                {
                    if ( tx.isActive () )
                    {
                        tx.rollback ();
                    }
                    throw new RuntimeException ( e );
                }
            } );
        }
        catch ( final Exception e )
        {
            throw new RuntimeException ( e );
        }
    }
}
