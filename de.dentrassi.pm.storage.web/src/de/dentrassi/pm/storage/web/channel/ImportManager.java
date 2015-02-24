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
package de.dentrassi.pm.storage.web.channel;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import de.dentrassi.osgi.job.JobHandle;
import de.dentrassi.osgi.job.JobManager;
import de.dentrassi.pm.importer.Importer;
import de.dentrassi.pm.importer.job.ImporterJobConfiguration;
import de.dentrassi.pm.importer.web.ImportDescriptor;
import de.dentrassi.pm.importer.web.ImportRequest;

public class ImportManager
{
    private final ServiceTracker<Importer, Importer> tracker;

    private final JobManager jobManager;

    public ImportManager ( final BundleContext context, final JobManager jobManager )
    {
        this.tracker = new ServiceTracker<> ( context, Importer.class, null );
        this.tracker.open ();

        this.jobManager = jobManager;
    }

    public Importer getImporter ( final String id )
    {
        for ( final Importer imp : this.tracker.getTracked ().values () )
        {
            if ( id.equals ( imp.getDescription ().getId () ) )
            {
                return imp;
            }
        }
        return null;
    }

    public void dispose ()
    {
        this.tracker.close ();
    }

    public JobHandle perform ( final ImportDescriptor descriptor, final ImportRequest request )
    {
        final String importerId = request.getImporterId ();
        final Importer imp = getImporter ( importerId );

        if ( imp == null )
        {
            throw new IllegalArgumentException ( String.format ( "Importer '%s' could not be found'", importerId ) );
        }

        final ImporterJobConfiguration cfg = new ImporterJobConfiguration ( descriptor, request.getImporterId (), request.getConfiguration () );

        return this.jobManager.startJob ( "package.drone.importer", cfg );
    }

}
