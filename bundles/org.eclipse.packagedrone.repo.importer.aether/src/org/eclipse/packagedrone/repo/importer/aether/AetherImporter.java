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
package org.eclipse.packagedrone.repo.importer.aether;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.aether.RepositoryException;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResult;
import org.eclipse.aether.util.filter.DependencyFilterUtils;
import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.importer.ImportContext;
import org.eclipse.packagedrone.repo.importer.ImportSubContext;
import org.eclipse.packagedrone.repo.importer.Importer;
import org.eclipse.packagedrone.repo.importer.ImporterDescription;
import org.eclipse.packagedrone.repo.importer.SimpleImporterDescription;
import org.eclipse.packagedrone.web.LinkTarget;
import org.eclipse.scada.utils.io.RecursiveDeleteVisitor;

import com.google.gson.GsonBuilder;

public class AetherImporter implements Importer
{

    public static final String ID = "aether";

    private static final SimpleImporterDescription DESCRIPTION = new SimpleImporterDescription ();

    static
    {
        DESCRIPTION.setId ( ID );
        DESCRIPTION.setLabel ( "Maven 2 Importer" );
        DESCRIPTION.setDescription ( "Import artifacts from Maven Repositories using Eclipse Aether" );
        DESCRIPTION.setStartTarget ( new LinkTarget ( "/import/{token}/aether/start" ) );
    }

    private final GsonBuilder gsonBuilder;

    public AetherImporter ()
    {
        this.gsonBuilder = new GsonBuilder ();
    }

    @Override
    public ImporterDescription getDescription ()
    {
        return DESCRIPTION;
    }

    @Override
    public void runImport ( final ImportContext context, final String configuration ) throws Exception
    {
        final ImportConfiguration cfg = this.gsonBuilder.create ().fromJson ( configuration, ImportConfiguration.class );
        runImport ( context, cfg );
    }

    private void runImport ( final ImportContext context, final ImportConfiguration cfg ) throws Exception
    {
        final Path tmpDir = Files.createTempDirectory ( "aether" );

        context.addCleanupTask ( () -> {
            Files.walkFileTree ( tmpDir, new RecursiveDeleteVisitor () );
            Files.deleteIfExists ( tmpDir );
        } );

        final Collection<ArtifactResult> results = process ( tmpDir, cfg );

        final List<ArtifactResult> later = new LinkedList<> ();
        final Map<String, ImportSubContext> roots = new HashMap<> ();

        for ( final ArtifactResult result : results )
        {
            if ( !result.isResolved () )
            {
                continue;
            }

            importArtifact ( context, result, roots, later );
        }

        // try sub artifact again

        for ( final ArtifactResult result : later )
        {
            importArtifact ( context, result, roots, null );
        }
    }

    private void importArtifact ( final ImportContext context, final ArtifactResult result, final Map<String, ImportSubContext> roots, final List<ArtifactResult> later )
    {
        final Artifact artifact = result.getArtifact ();
        final String key = String.format ( "%s:%s:%s", artifact.getGroupId (), artifact.getArtifactId (), artifact.getBaseVersion () );

        final Map<MetaKey, String> metadata = makeMetaData ( artifact );

        if ( later != null && artifact.getClassifier () != null && !artifact.getClassifier ().isEmpty () )
        {
            final ImportSubContext sub = roots.get ( key );
            if ( sub == null )
            {
                later.add ( result );
            }
            else
            {
                sub.scheduleImport ( artifact.getFile ().toPath (), false, artifact.getFile ().getName (), metadata );
            }
        }
        else
        {
            final ImportSubContext sub = context.scheduleImport ( artifact.getFile ().toPath (), false, artifact.getFile ().getName (), metadata );
            roots.put ( key, sub );
        }
    }

    private static Map<MetaKey, String> makeMetaData ( final Artifact artifact )
    {
        final Map<MetaKey, String> md = new HashMap<> ();

        md.put ( new MetaKey ( "mvn", "groupId" ), artifact.getGroupId () );
        md.put ( new MetaKey ( "mvn", "artifactId" ), artifact.getArtifactId () );
        md.put ( new MetaKey ( "mvn", "version" ), artifact.getVersion () );
        md.put ( new MetaKey ( "mvn", "extension" ), artifact.getExtension () );
        if ( artifact.getClassifier () != null )
        {
            md.put ( new MetaKey ( "mvn", "classifier" ), artifact.getClassifier () );
        }

        return md;
    }

    public static Collection<ArtifactResult> processDependencies ( final Path tmpDir, final ImportConfiguration cfg ) throws RepositoryException
    {
        final RepositorySystem system = Helper.newRepositorySystem ();
        final RepositorySystemSession session = Helper.newRepositorySystemSession ( tmpDir, system );

        final List<RemoteRepository> repositories;
        if ( cfg.getRepositoryUrl () == null || cfg.getRepositoryUrl ().isEmpty () )
        {
            repositories = Arrays.asList ( Helper.newCentralRepository () );
        }
        else
        {
            repositories = Arrays.asList ( Helper.newRemoteRepository ( "drone.aether.import", cfg.getRepositoryUrl () ) );
        }

        // add all coordinates

        final CollectRequest cr = new CollectRequest ();
        cr.addRepository ( repositories.get ( 0 ) );
        for ( final MavenCoordinates coords : cfg.getCoordinates () )
        {
            final Dependency dep = new Dependency ( new DefaultArtifact ( coords.toString () ), "compile" );
            cr.addDependency ( dep );
        }

        final DependencyFilter filter = DependencyFilterUtils.classpathFilter ( "compile" );
        final DependencyRequest deps = new DependencyRequest ( cr, filter );

        // resolve

        final DependencyResult dr = system.resolveDependencies ( session, deps );
        final List<ArtifactResult> arts = dr.getArtifactResults ();

        if ( !cfg.isIncludeSources () )
        {
            return arts;
        }

        final List<ArtifactRequest> reqs = new ArrayList<> ( arts.size () * 2 );
        for ( final ArtifactResult ar : arts )
        {
            reqs.add ( ar.getRequest () );

            final DefaultArtifact sources = makeSources ( ar.getArtifact () );
            if ( sources != null )
            {
                reqs.add ( makeRequest ( repositories, sources ) );
            }
        }

        return system.resolveArtifacts ( session, reqs );
    }

    public static Collection<ArtifactResult> process ( final Path tmpDir, final ImportConfiguration cfg ) throws ArtifactResolutionException
    {
        final RepositorySystem system = Helper.newRepositorySystem ();
        final RepositorySystemSession session = Helper.newRepositorySystemSession ( tmpDir, system );

        final List<RemoteRepository> repositories;
        if ( cfg.getRepositoryUrl () == null || cfg.getRepositoryUrl ().isEmpty () )
        {
            repositories = Arrays.asList ( Helper.newCentralRepository () );
        }
        else
        {
            repositories = Arrays.asList ( Helper.newRemoteRepository ( "drone.aether.import", cfg.getRepositoryUrl () ) );
        }

        final Collection<ArtifactRequest> requests = new LinkedList<> ();

        for ( final MavenCoordinates coords : cfg.getCoordinates () )
        {
            // main artifact

            final DefaultArtifact main = new DefaultArtifact ( coords.toString () );
            requests.add ( makeRequest ( repositories, main ) );

            final DefaultArtifact sources = makeSources ( main );
            if ( sources != null )
            {
                requests.add ( makeRequest ( repositories, sources ) );
            }
        }

        // process

        return system.resolveArtifacts ( session, requests );
    }

    private static DefaultArtifact makeSources ( final Artifact main )
    {
        if ( main.getClassifier () != null && !main.getClassifier ().isEmpty () )
        {
            return null;
        }

        return new DefaultArtifact ( main.getGroupId (), main.getArtifactId (), "sources", main.getExtension (), main.getVersion () );
    }

    private static ArtifactRequest makeRequest ( final List<RemoteRepository> repositories, final Artifact artifact )
    {
        final ArtifactRequest artifactRequest = new ArtifactRequest ();
        artifactRequest.setArtifact ( artifact );
        artifactRequest.setRepositories ( repositories );
        return artifactRequest;
    }
}
