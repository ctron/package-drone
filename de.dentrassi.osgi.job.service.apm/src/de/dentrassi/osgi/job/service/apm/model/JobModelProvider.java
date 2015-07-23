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
package de.dentrassi.osgi.job.service.apm.model;

import de.dentrassi.pm.apm.AbstractSimpleStorageModelProvider;
import de.dentrassi.pm.apm.StorageContext;

public class JobModelProvider extends AbstractSimpleStorageModelProvider<JobModel, JobWriteModel>
{

    private JobWriteModel writeModel;

    @Override
    public JobWriteModel cloneWriteModel ()
    {
        return new JobWriteModel ( this.writeModel.makeJobMap () );
    }

    @Override
    protected void updateWriteModel ( final JobWriteModel writeModel )
    {
        super.updateWriteModel ( writeModel );
        this.writeModel = writeModel;
    }

    @Override
    protected void persistWriteModel ( final StorageContext context, final JobWriteModel writeModel ) throws Exception
    {
        // right now we don't persist jobs
    }

    @Override
    protected JobModel renderViewModel ( final JobWriteModel writeModel )
    {
        return new JobModel ( writeModel.makeJobMap ().values () );
    }

    @Override
    protected JobWriteModel loadWriteModel ( final StorageContext context ) throws Exception
    {
        return new JobWriteModel ();
    }

}
