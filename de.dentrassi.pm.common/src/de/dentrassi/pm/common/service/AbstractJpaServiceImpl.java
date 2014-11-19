package de.dentrassi.pm.common.service;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

public class AbstractJpaServiceImpl
{
    private EntityManagerFactory entityManagerFactory;

    @FunctionalInterface
    public static interface ManagerFunction<T>
    {
        public T process ( EntityManager entityManager ) throws Exception;
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

    protected <T> T doWithManager ( final ManagerFunction<T> function ) throws Exception
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
        try
        {
            doWithManager ( entityManager -> {

                final EntityTransaction tx = entityManager.getTransaction ();
                tx.begin ();

                try
                {
                    function.processVoid ( entityManager );
                    tx.commit ();
                    return null;
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

    protected <T> T doWithTransaction ( final ManagerFunction<T> function )
    {
        try
        {
            return doWithManager ( entityManager -> {

                final EntityTransaction tx = entityManager.getTransaction ();
                tx.begin ();

                try
                {
                    final T result = function.process ( entityManager );
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
