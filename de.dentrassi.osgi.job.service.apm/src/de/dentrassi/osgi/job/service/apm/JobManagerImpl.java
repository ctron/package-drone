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
package de.dentrassi.osgi.job.service.apm;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dentrassi.osgi.job.ErrorInformation;
import de.dentrassi.osgi.job.JobFactory;
import de.dentrassi.osgi.job.JobFactoryDescriptor;
import de.dentrassi.osgi.job.JobHandle;
import de.dentrassi.osgi.job.JobInstance;
import de.dentrassi.osgi.job.JobInstance.Context;
import de.dentrassi.osgi.job.JobManager;
import de.dentrassi.osgi.job.JobRequest;
import de.dentrassi.osgi.job.State;
import de.dentrassi.osgi.job.service.apm.model.JobInstanceEntity;
import de.dentrassi.osgi.job.service.apm.model.JobModel;
import de.dentrassi.osgi.job.service.apm.model.JobModelProvider;
import de.dentrassi.osgi.job.service.apm.model.JobWriteModel;
import de.dentrassi.pm.apm.StorageManager;
import de.dentrassi.pm.apm.StorageRegistration;
import de.dentrassi.pm.common.MetaKey;

public class JobManagerImpl implements JobManager
{
    public class ContextImpl implements Context, Runnable
    {
        private final String id;

        private long totalAmount;

        private long worked;

        private String label;

        private final JobFactory factory;

        private final String data;

        public ContextImpl ( final String id, final JobFactory factory, final String data )
        {
            this.id = id;
            this.factory = factory;
            this.data = data;
        }

        @Override
        public void run ()
        {
            try
            {
                final JobInstance instance = this.factory.createInstance ( this.data );
                internalSetRunning ( this.id );
                instance.run ( this );
            }
            catch ( final Throwable e )
            {
                logger.debug ( "Failed to run job", e );
                internalSetError ( this.id, e );
            }
            finally
            {
                internalSetComplete ( this.id );
            }
        }

        @Override
        public void beginWork ( final String label, final long amount )
        {
            this.totalAmount = amount;
            this.label = label;
            internalStartWork ( this.id, label );
        }

        @Override
        public void setCurrentTaskName ( final String name )
        {
            internalSetTaskLabel ( this.id, name == null ? this.label : String.format ( "%s: %s", this.label, name ) );
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

    private static final MetaKey MODEL_KEY = new MetaKey ( "scheduler", "jobs" );

    private StorageManager storageManager;

    private StorageRegistration handle;

    private final JobQueue queue = new JobQueue ();

    private final FactoryManager factoryManager;

    public JobManagerImpl ()
    {
        this.factoryManager = new FactoryManager ( FrameworkUtil.getBundle ( JobManagerImpl.class ).getBundleContext () );
    }

    public void setStorageManager ( final StorageManager storageManager )
    {
        this.storageManager = storageManager;
    }

    public void start ()
    {
        this.factoryManager.start ();
        this.queue.start ();
        this.handle = this.storageManager.registerModel ( 10_000, MODEL_KEY, new JobModelProvider () );
    }

    public void stop ()
    {
        this.queue.stop ();
        if ( this.handle != null )
        {
            this.handle.unregister ();
            this.handle = null;
        }
        this.factoryManager.stop ();
    }

    @Override
    public JobHandle startJob ( final JobRequest job )
    {
        logger.debug ( "Start job: {}", job );

        final String factoryId = job.getFactoryId ();

        final Optional<JobFactory> factory = getJobFactory ( factoryId );

        if ( !factory.isPresent () )
        {
            throw new IllegalArgumentException ( String.format ( "Job factory '%s' is unknown", factoryId ) );
        }

        return internalStartJob ( factory.get (), factoryId, job.getData () );
    }

    /**
     * @deprecated should use {@link JobManagerImpl#startJob(String, String)}
     *             instead
     */
    @Override
    @Deprecated
    public JobHandle startJob ( final String factoryId, final Object data )
    {
        logger.debug ( "Start job: {} - {}", factoryId, data );

        final Optional<JobFactory> factory = getJobFactory ( factoryId );

        if ( !factory.isPresent () )
        {
            throw new IllegalArgumentException ( String.format ( "Job factory '%s' is unknown", factoryId ) );
        }

        return internalStartJob ( factory.get (), factoryId, factory.get ().encodeConfiguration ( data ) );
    }

    @Override
    public Collection<? extends JobHandle> getActiveJobs ()
    {
        return this.storageManager.accessCall ( MODEL_KEY, JobModel.class, jobs -> {
            return jobs.getJobs ().values ().stream ().filter ( job -> !job.isComplete () ).collect ( Collectors.toList () );
        } );
    }

    @Override
    public JobHandle getJob ( final String id )
    {
        logger.trace ( "Get job: {}", id );

        return this.storageManager.accessCall ( MODEL_KEY, JobModel.class, jobs -> jobs.getJobs ().get ( id ) );
    }

    protected Optional<JobFactory> getJobFactory ( final String factoryId )
    {
        return this.factoryManager.getFactory ( factoryId );
    }

    @Override
    public JobFactoryDescriptor getFactory ( final String factoryId )
    {
        return getJobFactory ( factoryId ).map ( JobFactory::getDescriptor ).orElse ( null );
    }

    private JobHandle internalStartJob ( final JobFactory factory, final String factoryId, final String data )
    {
        final String id = makeId ();

        final JobHandle handle = this.storageManager.modifyCall ( MODEL_KEY, JobWriteModel.class, jobs -> {

            final JobInstanceEntity ji = new JobInstanceEntity ();

            ji.setId ( id );
            ji.setFactoryId ( factoryId );
            ji.setData ( data );
            ji.setState ( State.SCHEDULED );
            ji.setLabel ( factory.makeLabel ( data ) );

            StorageManager.executeAfterPersist ( () -> {
                this.queue.push ( new ContextImpl ( id, factory, data ) );
            } );

            jobs.addJob ( ji );

            return new JobHandleImpl ( ji );
        } );

        return handle;
    }

    private static String makeId ()
    {
        return UUID.randomUUID ().toString ();
    }

    protected void modifyJob ( final String id, final Consumer<JobInstanceEntity> consumer )
    {
        this.storageManager.modifyRun ( MODEL_KEY, JobWriteModel.class, ( jobs ) -> {
            final JobInstanceEntity ji = jobs.getJobForUpdate ( id );
            if ( ji == null )
            {
                throw new IllegalStateException ( String.format ( "Unable to find job '%s'", id ) );
            }
            consumer.accept ( ji );
        } );
    }

    protected void internalSetError ( final String id, final Throwable e )
    {
        final ErrorInformation error = ErrorInformation.createFrom ( e );

        logger.debug ( "{}: set error: {}", id, error );

        modifyJob ( id, job -> job.setErrorInformation ( error ) );
    }

    public void internalSetResult ( final String id, final String data )
    {
        logger.trace ( "{}: set result: {}", id, data );

        modifyJob ( id, job -> job.setResult ( data ) );
    }

    public void internalSetRunning ( final String id )
    {
        logger.debug ( "{}: set running", id );

        modifyJob ( id, job -> job.setState ( State.RUNNING ) );
    }

    public void internalSetComplete ( final String id )
    {
        logger.debug ( "{}: set complete", id );

        modifyJob ( id, job -> job.setState ( State.COMPLETE ) );
    }

    public void internalWorked ( final String id, final double percentComplete )
    {
        logger.trace ( "{}: worked: {}", id, percentComplete );

        modifyJob ( id, job -> job.setPercentComplete ( percentComplete ) );
    }

    public void internalStartWork ( final String id, final String label )
    {
        logger.trace ( "{}: set start work: {}", id, label );

        modifyJob ( id, job -> {
            job.setCurrentWorkLabel ( label );
            job.setPercentComplete ( 0.0 );
        } );
    }

    public void internalSetTaskLabel ( final String id, final String label )
    {
        logger.trace ( "{}: set task label: {}", id, label );

        modifyJob ( id, job -> job.setCurrentWorkLabel ( label ) );
    }

}
