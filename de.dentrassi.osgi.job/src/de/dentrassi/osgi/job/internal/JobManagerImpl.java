/*******************************************************************************
 * Copyright (c) 2015 Jens Reimann.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jens Reimann - initial API and implementation
 *******************************************************************************/
package de.dentrassi.osgi.job.internal;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import de.dentrassi.osgi.job.Job;
import de.dentrassi.osgi.job.JobHandle;
import de.dentrassi.osgi.job.JobManager;
import de.dentrassi.osgi.job.State;

public class JobManagerImpl implements JobManager
{
    private class JobHandleImpl implements JobHandle
    {
        private final String id;

        private final Job job;

        private volatile State state = State.SCHEDULED;

        private Throwable error;

        public JobHandleImpl ( final String id, final Job job )
        {
            this.id = id;
            this.job = job;
        }

        @Override
        public String getId ()
        {
            return this.id;
        }

        @Override
        public String toString ()
        {
            return String.format ( "[Job: %s, %s]", this.id, this.state );
        }

        @Override
        public State getState ()
        {
            return this.state;
        }

        @Override
        public Job getJob ()
        {
            return this.job;
        }

        public void run ()
        {
            try
            {
                this.state = State.RUNNING;
                this.job.run ();
            }
            catch ( final Throwable e )
            {
                this.error = e;
            }
            finally
            {
                this.state = State.COMPLETE;
            }
        }

        @Override
        public Throwable getError ()
        {
            return this.error;
        }

        @Override
        public boolean isComplete ()
        {
            return getState () == State.COMPLETE;
        }
    }

    private final Map<String, JobHandleImpl> jobs = new ConcurrentHashMap<> ();

    @Override
    public JobHandle startJob ( final Job job )
    {
        final JobHandleImpl result = new JobHandleImpl ( UUID.randomUUID ().toString (), job );

        this.jobs.put ( result.getId (), result );

        runJob ( result );

        return result;
    }

    private void runJob ( final JobHandleImpl handle )
    {
        final Thread thread = new Thread ( "JobManager/" + handle.getId () ) {
            @Override
            public void run ()
            {
                handle.run ();
            }
        };
        thread.start ();
    }

    @Override
    public Collection<? extends JobHandle> getJobs ()
    {
        return this.jobs.values ();
    }

    @Override
    public JobHandle getJob ( final String id )
    {
        return this.jobs.get ( id );
    }

}
