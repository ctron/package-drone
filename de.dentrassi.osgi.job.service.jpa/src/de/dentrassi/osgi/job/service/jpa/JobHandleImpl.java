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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.GsonBuilder;

import de.dentrassi.osgi.job.ErrorInformation;
import de.dentrassi.osgi.job.JobHandle;
import de.dentrassi.osgi.job.JobRequest;
import de.dentrassi.osgi.job.State;
import de.dentrassi.osgi.job.jpa.JobInstanceEntity;

public class JobHandleImpl implements JobHandle
{
    private final static Logger logger = LoggerFactory.getLogger ( JobHandleImpl.class );

    private final String id;

    private final State state;

    private final String errorString;

    private ErrorInformation errorInformation;

    private final JobRequest request;

    private final String label;

    private final String result;

    public JobHandleImpl ( final JobInstanceEntity ji )
    {
        this.id = ji.getId ();
        this.state = ji.getState ();
        this.errorString = ji.getErrorInformation ();

        this.request = new JobRequest ();
        this.request.setFactoryId ( ji.getFactoryId () );
        this.request.setData ( ji.getData () );

        this.label = ji.getLabel ();
        this.result = ji.getResult ();
    }

    @Override
    public String getResult ()
    {
        return this.result;
    }

    @Override
    public String getId ()
    {
        return this.id;
    }

    @Override
    public State getState ()
    {
        return this.state;
    }

    @Override
    public ErrorInformation getError ()
    {
        if ( this.errorInformation == null && this.errorString != null )
        {
            try
            {
                this.errorInformation = new GsonBuilder ().create ().fromJson ( this.errorString, ErrorInformation.class );
            }
            catch ( final Exception e )
            {
                logger.debug ( "Failed to decode error information" );
            }
        }
        return this.errorInformation;
    }

    @Override
    public JobRequest getRequest ()
    {
        return this.request;
    }

    @Override
    public String getLabel ()
    {
        return this.label;
    }

    @Override
    public boolean isComplete ()
    {
        return getState () == State.COMPLETE;
    }
}
