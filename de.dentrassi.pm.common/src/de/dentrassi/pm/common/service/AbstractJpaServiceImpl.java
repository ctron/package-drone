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
package de.dentrassi.pm.common.service;

import java.util.function.Consumer;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

/**
 * A base class for creating services based on JPA
 */
public class AbstractJpaServiceImpl
{
    protected EntityManagerFactory entityManagerFactory;

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

    protected void doWithTransactionVoid ( final VoidManagerFunction function, final Guard... guards )
    {
        doWithTransaction ( em -> {
            function.processVoid ( em );
            return null;
        }, guards );
    }

    protected <R> R doWithTransaction ( final ManagerFunction<R, EntityManager> function, final Guard... guards )
    {
        try
        {
            return doWithManager ( entityManager -> {

                // start the transaction
                final EntityTransaction tx = entityManager.getTransaction ();
                tx.begin ();

                try
                {
                    // run "before" guards
                    runGuards ( guards, guard -> guard.before ( entityManager ) );

                    // make the call
                    final R result = function.process ( entityManager );

                    // run "beforeCommit" guards
                    runGuards ( guards, guard -> guard.beforeCommit ( result, entityManager ) );

                    // commit
                    tx.commit ();

                    // run "afterCommit" guards
                    runGuards ( guards, Guard::afterCommit );

                    return result;
                }
                catch ( final Exception e )
                {
                    // run "beforeRollback" guards
                    runGuards ( guards, guard -> guard.beforeRollback ( e, entityManager ) );

                    try
                    {
                        // if a transaction is active -> roll back
                        if ( tx.isActive () )
                        {
                            tx.rollback ();
                        }
                    }
                    finally
                    {
                        // run "afterRollback" guards
                        runGuards ( guards, Guard::afterRollback );
                    }

                    // throw exception
                    throw new RuntimeException ( e );
                }
                finally
                {
                    // run "afterAll" guards
                    runGuards ( guards, Guard::afterAll );
                }
            } );
        }
        catch ( final Exception e )
        {
            throw new RuntimeException ( e );
        }
    }

    private void runGuards ( final Guard[] guards, final Consumer<Guard> consumer )
    {
        for ( final Guard guard : guards )
        {
            consumer.accept ( guard );
        }
    }
}
