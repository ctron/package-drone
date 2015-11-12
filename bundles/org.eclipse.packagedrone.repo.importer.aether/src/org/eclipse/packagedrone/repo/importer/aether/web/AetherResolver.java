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
package org.eclipse.packagedrone.repo.importer.aether.web;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import org.eclipse.aether.repository.ArtifactRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.packagedrone.job.AbstractJsonJobFactory;
import org.eclipse.packagedrone.job.JobFactoryDescriptor;
import org.eclipse.packagedrone.job.JobInstance.Context;
import org.eclipse.packagedrone.repo.importer.aether.AetherImporter;
import org.eclipse.packagedrone.repo.importer.aether.ImportConfiguration;
import org.eclipse.packagedrone.repo.importer.aether.MavenCoordinates;
import org.eclipse.packagedrone.web.LinkTarget;
import org.eclipse.scada.utils.io.RecursiveDeleteVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AetherResolver extends AbstractJsonJobFactory<ImportConfiguration, AetherResult>
{
    private final static Logger logger = LoggerFactory.getLogger ( AetherResolver.class );

    public static final String ID = "org.eclipse.packagedrone.repo.importer.aether.web.resolver";

    private static final JobFactoryDescriptor DESCRIPTOR = new JobFactoryDescriptor () {

        @Override
        public LinkTarget getResultTarget ()
        {
            return null;
        }
    };

    public AetherResolver ()
    {
        super ( ImportConfiguration.class );
    }

    @Override
    protected String makeLabelFromData ( final ImportConfiguration data )
    {
        String label = "";

        if ( !data.getCoordinates ().isEmpty () )
        {
            label = data.getCoordinates ().get ( 0 ).toString ();
            if ( data.getCoordinates ().size () > 1 )
            {
                label += String.format ( " (and %s more)", data.getCoordinates ().size () - 1 );
            }
        }

        return String.format ( "Resolve maven dependencies: %s", label );
    }

    @Override
    public JobFactoryDescriptor getDescriptor ()
    {
        return DESCRIPTOR;
    }

    @Override
    protected AetherResult process ( final Context context, final ImportConfiguration cfg ) throws Exception
    {
        final AetherResult result = new AetherResult ();

        final Path tmpDir = Files.createTempDirectory ( "aether" );

        try
        {
            final Collection<ArtifactResult> results = AetherImporter.processDependencies ( tmpDir, cfg );

            for ( final ArtifactResult ar : results )
            {
                final AetherResult.Entry entry = new AetherResult.Entry ();
                entry.setCoordinates ( MavenCoordinates.fromResult ( ar ) );
                entry.setResolved ( ar.isResolved () );

                result.getArtifacts ().add ( entry );
            }

            Collections.sort ( result.getArtifacts (), Comparator.comparing ( AetherResult.Entry::getCoordinates ) );

            if ( !results.isEmpty () )
            {
                final ArtifactRepository repo = results.iterator ().next ().getRepository ();
                if ( repo instanceof RemoteRepository )
                {
                    final RemoteRepository remRepo = (RemoteRepository)repo;
                    result.setRepositoryUrl ( remRepo.getUrl () );
                }
            }
        }
        catch ( final Exception e )
        {
            logger.warn ( "Failed to test", e );
            throw e;
        }
        finally
        {
            Files.walkFileTree ( tmpDir, new RecursiveDeleteVisitor () );
            Files.deleteIfExists ( tmpDir );
        }

        return result;
    }

}
