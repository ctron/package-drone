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
package de.dentrassi.osgi.job.web;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import de.dentrassi.osgi.job.Job;
import de.dentrassi.osgi.job.JobHandle;
import de.dentrassi.osgi.job.web.internal.Activator;

public class Jobs
{
    public static final String SESSION_ATTRIBUTE_JOBS = Jobs.class.getName () + ".jobs";

    public static class SessionJobs
    {
        private final Map<String, JobHandle> jobs = new HashMap<> ();

        public void add ( final JobHandle handle )
        {
            this.jobs.put ( handle.getId (), handle );
        }

        public void remove ( final JobHandle handle )
        {
            this.jobs.remove ( handle.getId () );
        }

        public JobHandle get ( final String id )
        {
            final JobHandle job = this.jobs.get ( id );
            if ( job != null && job.isComplete () )
            {
                this.jobs.remove ( id );
            }
            return job;
        }
    }

    public static JobHandle startJob ( final HttpServletRequest request, final Job job )
    {
        final JobHandle handle = Activator.getJobManager ().startJob ( job );

        // add to session
        getSessionJobs ( request ).add ( handle );

        return handle;
    }

    protected static SessionJobs getSessionJobs ( final HttpServletRequest request )
    {
        final HttpSession session = request.getSession ();

        Object jobsObject = session.getAttribute ( SESSION_ATTRIBUTE_JOBS );
        if ( ! ( jobsObject instanceof SessionJobs ) )
        {
            jobsObject = new SessionJobs ();
            session.setAttribute ( SESSION_ATTRIBUTE_JOBS, jobsObject );
        }
        return (SessionJobs)jobsObject;
    }

    public static JobHandle start ( final HttpServletRequest request, final String label, final Runnable run )
    {
        return startJob ( request, new Job () {

            @Override
            public String getLabel ()
            {
                return label;
            }

            @Override
            public void run () throws Exception
            {
                run.run ();
            }
        } );
    }

    public static JobHandle start ( final HttpServletRequest request, final String label, final Callable<?> run )
    {
        return startJob ( request, new Job () {

            @Override
            public String getLabel ()
            {
                return label;
            }

            @Override
            public void run () throws Exception
            {
                run.call ();
            }
        } );
    }

    public static JobHandle get ( final HttpServletRequest request, final String id )
    {
        return getSessionJobs ( request ).get ( id );
    }
}
