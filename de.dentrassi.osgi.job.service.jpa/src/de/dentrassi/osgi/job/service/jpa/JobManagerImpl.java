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
package de.dentrassi.osgi.job.service.jpa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import java.util.function.Supplier;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.TypedQuery;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.dentrassi.osgi.job.ErrorInformation;
import de.dentrassi.osgi.job.JobFactory;
import de.dentrassi.osgi.job.JobFactoryDescriptor;
import de.dentrassi.osgi.job.JobHandle;
import de.dentrassi.osgi.job.JobInstance;
import de.dentrassi.osgi.job.JobInstance.Context;
import de.dentrassi.osgi.job.JobManager;
import de.dentrassi.osgi.job.JobRequest;
import de.dentrassi.osgi.job.State;
import de.dentrassi.osgi.job.jpa.JobInstanceEntity;
import de.dentrassi.pm.common.service.AbstractJpaServiceImpl;

public class JobManagerImpl extends AbstractJpaServiceImpl implements JobManager
{
    public class ContextImpl implements Context
    {
        private final String id;

        private long totalAmount;

        private long worked;

        public ContextImpl ( final String id )
        {
            this.id = id;
        }

        @Override
        public void beginWork ( final String label, final long amount )
        {
            this.totalAmount = amount;
            internalStartWork ( this.id, label );
        }

        @Override
        public void complete ()
        {
            internalWorked ( this.id, 1.0 );
        }

        @Override
        public void worked ( final long amount )
        {
            this.worked = Math.min ( this.worked + amount, this.totalAmount );
            internalWorked ( this.id, (double)this.worked / (double)this.totalAmount );
        }

        @Override
        public void setResult ( final String data )
        {
            logger.debug ( "Setting result for job {}: {}", this.id, data );
            internalSetResult ( this.id, data );
        }
    }

    private final static Logger logger = LoggerFactory.getLogger ( JobManagerImpl.class );

    private final Map<String, JobFactory> factories = new HashMap<> ();

    private final BundleContext context;

    private final ReadLock readLock;

    private final WriteLock writeLock;

    private final Gson gson;

    public JobManagerImpl ()
    {
        this.gson = new GsonBuilder ().create ();

        this.context = FrameworkUtil.getBundle ( JobManagerImpl.class ).getBundleContext ();
        final ReentrantReadWriteLock lock = new ReentrantReadWriteLock ();
        this.readLock = lock.readLock ();
        this.writeLock = lock.writeLock ();
    }

    protected void doLockedVoid ( final Lock lock, final Runnable run )
    {
        lock.lock ();
        try
        {
            run.run ();
        }
        finally
        {
            lock.unlock ();
        }
    }

    protected <V> V doLocked ( final Lock lock, final Supplier<V> run )
    {
        lock.lock ();
        try
        {
            return run.get ();
        }
        finally
        {
            lock.unlock ();
        }
    }

    public void addJobFactory ( final ServiceReference<JobFactory> reference )
    {
        doLockedVoid ( this.writeLock, ( ) -> {
            final Object factoryId = reference.getProperty ( JobFactory.FACTORY_ID );

            if ( ! ( factoryId instanceof String ) )
            {
                return;
            }

            if ( this.factories.containsKey ( factoryId ) )
            {
                return;
            }

            final JobFactory factory = this.context.getService ( reference );

            logger.info ( "Add job factory - factoryId: {}, factory: {}", factoryId, factory );

            if ( factoryId instanceof String )
            {
                this.factories.put ( (String)factoryId, factory );
            }
        } );
    }

    public void removeJobFactory ( final ServiceReference<JobFactory> reference )
    {
        doLockedVoid ( this.writeLock, ( ) -> {
            final Object factoryId = reference.getProperty ( JobFactory.FACTORY_ID );

            JobFactory factory = null;
            if ( factoryId instanceof String )
            {
                factory = this.factories.remove ( factoryId );
            }

            if ( factory != null )
            {
                this.context.ungetService ( reference );
            }

            logger.info ( "Removed job factory - factoryId: {}, factory: {}", factoryId, factory );
        } );
    }

    @Override
    public JobHandle startJob ( final JobRequest jobRequest )
    {
        return doLocked ( this.readLock, ( ) -> {
            final JobFactory factory = this.factories.get ( jobRequest.getFactoryId () );
            if ( factory == null )
            {
                throw new IllegalStateException ( String.format ( "Factory '%s' is unknown", factory ) );
            }

            return internalStartJob ( jobRequest, factory );
        } );
    }

    @Override
    public JobHandle startJob ( final String factoryId, final Object data )
    {
        return doLocked ( this.readLock, ( ) -> {
            final JobFactory factory = this.factories.get ( factoryId );
            if ( factory == null )
            {
                throw new IllegalStateException ( String.format ( "Factory '%s' is unknown", factoryId ) );
            }

            final String str = factory.encodeConfiguration ( data );
            return internalStartJob ( new JobRequest ( factoryId, str ), factory );

        } );
    }

    private JobHandle internalStartJob ( final JobRequest request, final JobFactory factory )
    {
        // create job instance before writing to the database, if we fail, we fail before that

        final JobInstance instance;
        try
        {
            instance = factory.createInstance ( request.getData () );
        }
        catch ( final Exception e )
        {
            throw new RuntimeException ( "Failed to create job instance", e );
        }

        final JobHandle result = doWithTransaction ( ( em ) -> {

            final JobInstanceEntity ji = new JobInstanceEntity ();

            ji.setLabel ( factory.makeLabel ( request.getData () ) );
            ji.setFactoryId ( request.getFactoryId () );
            ji.setData ( request.getData () );
            ji.setState ( State.SCHEDULED );

            em.persist ( ji );
            em.flush ();

            return convert ( ji );
        } );

        // only start thread after the committed to the database

        forkJob ( result.getId (), request, instance );

        return result;
    }

    private void forkJob ( final String id, final JobRequest request, final JobInstance instance )
    {
        final Thread t = new Thread () {
            @Override
            public void run ()
            {
                internalSetRunning ( id );

                try
                {
                    instance.run ( new ContextImpl ( id ) );
                }
                catch ( final Throwable e )
                {
                    internalSetError ( id, e );
                }
                finally
                {
                    internalSetComplete ( id );
                }
            }
        };
        t.setName ( String.format ( "JobInstance/" + id + "/" + request.getFactoryId () ) );
        t.start ();
    }

    protected void internalStartWork ( final String id, final String label )
    {
        doWithTransactionVoid ( ( em ) -> {
            final JobInstanceEntity ji = em.find ( JobInstanceEntity.class, id, LockModeType.NONE );
            if ( ji == null )
            {
                return;
            }
            ji.setCurrentWorkLabel ( label );
            ji.setPercentComplete ( 0.0 );
            em.persist ( ji );
            em.flush ();
        } );
    }

    protected void internalWorked ( final String id, final double percentComplete )
    {
        doWithTransactionVoid ( ( em ) -> {
            final JobInstanceEntity ji = em.find ( JobInstanceEntity.class, id, LockModeType.OPTIMISTIC );
            if ( ji == null )
            {
                return;
            }
            ji.setPercentComplete ( percentComplete );
            em.persist ( ji );
            em.flush ();
        } );
    }

    protected void internalSetRunning ( final String id )
    {
        doWithTransactionVoid ( ( em ) -> {
            final JobInstanceEntity ji = em.find ( JobInstanceEntity.class, id, LockModeType.OPTIMISTIC );
            if ( ji == null )
            {
                return;
            }
            if ( ji.getState () == State.SCHEDULED )
            {
                ji.setState ( State.RUNNING );
                em.persist ( ji );
                em.flush ();
            }
        } );
    }

    protected void internalSetComplete ( final String id )
    {
        doWithTransactionVoid ( ( em ) -> {
            final JobInstanceEntity ji = em.find ( JobInstanceEntity.class, id, LockModeType.OPTIMISTIC );
            if ( ji == null )
            {
                return;
            }
            if ( ji.getState () != State.COMPLETE )
            {
                ji.setState ( State.COMPLETE );
                ji.setPercentComplete ( 1.0 );
                em.persist ( ji );
                em.flush ();
            }
        } );
    }

    protected void internalSetResult ( final String id, final String data )
    {
        doWithTransactionVoid ( ( em ) -> {
            final JobInstanceEntity ji = em.find ( JobInstanceEntity.class, id, LockModeType.OPTIMISTIC );
            if ( ji == null )
            {
                return;
            }

            if ( ji.getState () == State.RUNNING )
            {
                ji.setResult ( data );
                em.persist ( ji );
                em.flush ();
            }

        } );
    }

    protected void internalSetError ( final String id, final Throwable e )
    {
        logger.debug ( "Handle error for: " + id, e );

        doWithTransactionVoid ( ( em ) -> {
            final JobInstanceEntity ji = em.find ( JobInstanceEntity.class, id, LockModeType.OPTIMISTIC );
            if ( ji == null )
            {
                return;
            }

            if ( ji.getState () == State.RUNNING && e != null )
            {
                final ErrorInformation err = ErrorInformation.createFrom ( e );

                ji.setErrorInformation ( this.gson.toJson ( err ) );
                ji.setState ( State.COMPLETE );
                em.persist ( ji );
                em.flush ();
            }

        } );
    }

    @Override
    public Collection<? extends JobHandle> getActiveJobs ()
    {
        return doWithTransaction ( ( em ) -> processGetJobs ( em ) );
    }

    private Collection<? extends JobHandle> processGetJobs ( final EntityManager em )
    {
        final TypedQuery<JobInstanceEntity> q = em.createQuery ( String.format ( "SELECT ji from %s ji where ji.state in (0, 1)", JobInstanceEntity.class.getName () ), JobInstanceEntity.class );
        final List<JobInstanceEntity> resultList = q.getResultList ();

        final Collection<JobHandle> result = new ArrayList<> ( resultList.size () );
        for ( final JobInstanceEntity ji : resultList )
        {
            result.add ( convert ( ji ) );
        }
        return result;
    }

    @Override
    public JobHandle getJob ( final String id )
    {
        return doWithTransaction ( ( em ) -> {
            final JobInstanceEntity ji = em.find ( JobInstanceEntity.class, id );
            return convert ( ji );
        } );
    }

    private JobHandle convert ( final JobInstanceEntity ji )
    {
        if ( ji == null )
        {
            return null;
        }

        return new JobHandleImpl ( ji );
    }

    public void start ()
    {
    }

    public void stop ()
    {
    }

    @Override
    public JobFactoryDescriptor getFactory ( final String factoryId )
    {
        final JobFactory factory = this.factories.get ( factoryId );
        if ( factory == null )
        {
            return null;
        }
        return factory.getDescriptor ();
    }
}
