/*******************************************************************************
 * Copyright (c) 2014 Jens Reimann.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jens Reimann - initial API and implementation
 *******************************************************************************/
package de.dentrassi.pm.p2.servlet;

import static de.dentrassi.pm.common.XmlHelper.fixSize;

import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.io.ByteStreams;

import de.dentrassi.pm.osgi.BundleInformation;
import de.dentrassi.pm.osgi.BundleInformationParser;
import de.dentrassi.pm.storage.MetaKey;
import de.dentrassi.pm.storage.service.Artifact;
import de.dentrassi.pm.storage.service.Channel;

public class ArtifactsHandler extends AbstractRepositoryHandler
{
    public ArtifactsHandler ( final Channel channel )
    {
        super ( channel );
    }

    @Override
    public void prepare () throws Exception
    {
        final Document doc = initRepository ( "artifactRepository", "org.eclipse.equinox.p2.artifact.repository.simpleRepository" );
        final Element root = doc.getDocumentElement ();

        addProperties ( root );

        addMappings ( root );

        final Element artifacts = addElement ( root, "artifacts" );

        for ( final Artifact artifact : this.channel.getArtifacts () )
        {
            final Map<MetaKey, String> md = artifact.getMetaData ();

            final String name = artifact.getName ();
            final String mvnExtension = md.get ( new MetaKey ( "mvn", "extension" ) );

            if ( "jar".equals ( mvnExtension ) || name.endsWith ( ".jar" ) )
            {
                // need a different way to detect JARs
                attachP2Data ( artifact, artifacts, md );
            }
        }

        fixSize ( artifacts );

        setData ( doc );
    }

    private void addMappings ( final Element root )
    {
        final Element mappings = addElement ( root, "mappings" );

        addMapping ( mappings, "(& (classifier=osgi.bundle))", "${repoUrl}/plugins/${id}_${version}.jar" );
        addMapping ( mappings, "(& (classifier=binary))", "${repoUrl}/binary/${id}_${version}" );
        addMapping ( mappings, "(& (classifier=org.eclipse.update.feature))", "${repoUrl}/features/${id}_${version}.jar" );

        fixSize ( mappings );
    }

    private void addMapping ( final Element mappings, final String rule, final String output )
    {
        final Element m = addElement ( mappings, "rule" );
        m.setAttribute ( "filter", rule );
        m.setAttribute ( "output", output );
    }

    @FunctionalInterface
    public interface ArtifactProcessor
    {
        public void process ( Path file ) throws Exception;
    }

    private void processArtifact ( final Artifact artifact, final ArtifactProcessor processor ) throws Exception
    {
        final Path file = Files.createTempFile ( "blob-", null );
        try
        {
            artifact.streamData ( ( info, stream ) -> {
                try ( BufferedOutputStream out = new BufferedOutputStream ( new FileOutputStream ( file.toFile () ) ) )
                {
                    ByteStreams.copy ( stream, out );
                }
                try
                {
                    processor.process ( file );
                }
                catch ( final Exception e )
                {
                    // ignore
                }
            } );
        }
        finally
        {
            Files.deleteIfExists ( file );
        }
    }

    private void attachP2Data ( final Artifact artifact, final Element artifacts, final Map<MetaKey, String> md ) throws Exception
    {
        processArtifact ( artifact, file -> {
            extractBundleInformationFromJar ( file, artifacts, artifact, md );
            extractFeatureInformationFromJar ( file, artifacts, artifact, md );
        } );
    }

    private void extractFeatureInformationFromJar ( final Path file, final Element artifacts, final Artifact artifact, final Map<MetaKey, String> md ) throws Exception
    {
        Document fdoc;
        try ( ZipFile zf = new ZipFile ( file.toFile () ) )
        {
            final ZipEntry ze = zf.getEntry ( "feature.xml" );
            if ( ze == null )
            {
                return;
            }
            try ( InputStream stream = zf.getInputStream ( ze ) )
            {
                fdoc = this.xml.parse ( stream );
            }
        }

        // process feature content
        final Element root = fdoc.getDocumentElement ();
        if ( !"feature".equals ( root.getNodeName () ) )
        {
            return;
        }

        final String id = root.getAttribute ( "id" );
        final String version = root.getAttribute ( "version" );
        if ( id == null || version == null )
        {
            return;
        }

        final Element a = addElement ( artifacts, "artifact" );
        a.setAttribute ( "classifier", "org.eclipse.update.feature" );
        a.setAttribute ( "id", id );
        a.setAttribute ( "version", version );

        final String md5 = md.get ( new MetaKey ( "hasher", "md5" ) );

        final Element props = addElement ( a, "properties" );

        if ( md5 != null )
        {
            addProperty ( props, "download.md5", md5 );
        }

        addProperty ( props, "download.size", "" + artifact.getSize () );
        addProperty ( props, "artifact.size", "" + artifact.getSize () );
        addProperty ( props, "download.contentType", "application/zip" );
        addProperty ( props, "drone.artifact.id", artifact.getId () );
    }

    private void extractBundleInformationFromJar ( final Path file, final Element artifacts, final Artifact artifact, final Map<MetaKey, String> md ) throws IOException
    {
        final Manifest mf;

        try ( final JarInputStream jarStream = new JarInputStream ( new FileInputStream ( file.toFile () ) ) )
        {
            mf = jarStream.getManifest ();
        }

        if ( mf == null )
        {
            return;
        }

        final BundleInformation bi;
        try ( ZipFile zipFile = new ZipFile ( file.toFile () ) )
        {
            bi = new BundleInformationParser ( zipFile ).parse ();
            if ( bi == null )
            {
                return;
            }
        }
        catch ( final ZipException e )
        {
            return;
        }

        final Element a = addElement ( artifacts, "artifact" );
        a.setAttribute ( "classifier", "osgi.bundle" );
        a.setAttribute ( "id", bi.getId () );
        a.setAttribute ( "version", bi.getVersion () );

        final String md5 = md.get ( new MetaKey ( "hasher", "md5" ) );

        final Element props = addElement ( a, "properties" );

        if ( md5 != null )
        {
            addProperty ( props, "download.md5", md5 );
        }

        addProperty ( props, "download.size", "" + artifact.getSize () );
        addProperty ( props, "artifact.size", "" + artifact.getSize () );
        addProperty ( props, "drone.artifact.id", artifact.getId () );

        fixSize ( props );
    }

    private void addProperty ( final Element props, final String key, final String value )
    {
        final Element p = addElement ( props, "property" );
        p.setAttribute ( "name", key );
        p.setAttribute ( "value", value );
    }
}
