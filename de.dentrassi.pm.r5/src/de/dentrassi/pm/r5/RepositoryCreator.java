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
package de.dentrassi.pm.r5;

import static de.dentrassi.osgi.utils.Filters.and;
import static de.dentrassi.osgi.utils.Filters.pair;
import static de.dentrassi.osgi.utils.Filters.versionRange;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.osgi.framework.Version;

import de.dentrassi.osgi.utils.Filters;
import de.dentrassi.osgi.utils.Filters.Node;
import de.dentrassi.pm.VersionInformation;
import de.dentrassi.pm.aspect.common.osgi.OsgiAspectFactory;
import de.dentrassi.pm.aspect.common.spool.OutputSpooler;
import de.dentrassi.pm.aspect.common.spool.SpoolOutTarget;
import de.dentrassi.pm.common.ArtifactInformation;
import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.common.utils.IOConsumer;
import de.dentrassi.pm.osgi.bundle.BundleInformation;
import de.dentrassi.pm.osgi.bundle.BundleInformation.BundleRequirement;
import de.dentrassi.pm.osgi.bundle.BundleInformation.PackageExport;
import de.dentrassi.pm.osgi.bundle.BundleInformation.PackageImport;

public class RepositoryCreator
{
    private static final MetaKey KEY_SHA_256 = new MetaKey ( "hasher", "sha256" );

    private static final String FRAMEWORK_PACKAGE = "org.osgi.framework";

    private final OutputSpooler indexStreamBuilder;

    private final String name;

    private final Function<ArtifactInformation, String> urlProvider;

    public static interface Context
    {
        public void addArtifact ( ArtifactInformation artifact ) throws IOException;
    }

    private static class ContextImpl implements Context
    {
        private final XMLStreamWriter writer;

        private final Function<ArtifactInformation, String> urlProvider;

        public ContextImpl ( final XMLStreamWriter writer, final Function<ArtifactInformation, String> urlProvider )
        {
            this.writer = writer;
            this.urlProvider = urlProvider;
        }

        @Override
        public void addArtifact ( final ArtifactInformation art ) throws IOException
        {
            final Map<MetaKey, String> md = art.getMetaData ();

            final BundleInformation bi = OsgiAspectFactory.fetchBundleInformation ( md );

            if ( bi == null )
            {
                return;
            }

            if ( bi.getId () == null || bi.getVersion () == null )
            {
                return;
            }

            try
            {
                this.writer.writeStartElement ( "resource" );
                this.writer.writeCharacters ( "\n" );

                addIdentity ( this.writer, bi );
                addContent ( this.writer, art, this.urlProvider.apply ( art ) );
                addDependencies ( this.writer, bi );

                this.writer.writeEndElement ();
                this.writer.writeCharacters ( "\n\n" );
            }
            catch ( final XMLStreamException e )
            {
                throw new IOException ( e );
            }
        }
    }

    private static void addIdentity ( final XMLStreamWriter writer, final BundleInformation bi ) throws XMLStreamException
    {
        final Map<String, Object> caps = new HashMap<> ();

        caps.put ( "osgi.identity", bi.getId () );
        caps.put ( "version", bi.getVersion () );
        caps.put ( "type", "osgi.bundle" );

        addCapability ( writer, "osgi.identity", caps );
    }

    public static void addDependencies ( final XMLStreamWriter writer, final BundleInformation bi ) throws XMLStreamException
    {
        {
            final List<Node> nodes = new LinkedList<> ();

            for ( final String ee : bi.getRequiredExecutionEnvironments () )
            {
                nodes.add ( pair ( "osgi.ee", ee ) );
            }
            if ( !nodes.isEmpty () )
            {
                final Map<String, String> reqs = new HashMap<> ( 1 );
                reqs.put ( "filter", Filters.or ( nodes ) );
                addRequirement ( writer, "osgi.ee", reqs );
            }
        }

        {
            final Map<String, Object> caps = new HashMap<> ();

            caps.put ( "osgi.wiring.bundle", bi.getId () );
            caps.put ( "bundle-version", bi.getVersion () );

            addCapability ( writer, "osgi.wiring.bundle", caps );
        }

        {
            final Map<String, Object> caps = new HashMap<> ();

            caps.put ( "osgi.wiring.host", bi.getId () );
            caps.put ( "bundle-version", bi.getVersion () );

            addCapability ( writer, "osgi.wiring.host", caps );
        }

        for ( final BundleRequirement br : bi.getBundleRequirements () )
        {
            final Map<String, String> reqs = new HashMap<> ();

            final String filter = and ( //
            pair ( "osgi.wiring.bundle", br.getId () ), //
            versionRange ( "bundle-version", br.getVersionRange () ) //
            );

            reqs.put ( "filter", filter );

            if ( br.isOptional () )
            {
                reqs.put ( "resolution", "optional" );
            }

            addRequirement ( writer, "osgi.wiring.bundle", reqs );
        }

        for ( final PackageExport pe : bi.getPackageExports () )
        {
            final Map<String, Object> caps = new HashMap<> ();

            caps.put ( "osgi.wiring.package", pe.getName () );

            if ( pe.getVersion () != null )
            {
                caps.put ( "version", pe.getVersion () );
            }

            addCapability ( writer, "osgi.wiring.package", caps );

            // Add a 'osgi.contract' capability if this bundle is a framework package
            if ( FRAMEWORK_PACKAGE.equals ( pe.getName () ) )
            {
                final Version specVersion = mapFrameworkPackageVersion ( pe.getVersion () );
                if ( specVersion != null )
                {
                    final Map<String, Object> frameworkCaps = new HashMap<> ();
                    frameworkCaps.put ( "osgi.contract", "OSGiFramework" );
                    frameworkCaps.put ( "version", specVersion );
                    addCapability ( writer, "osgi.contract", frameworkCaps );
                }
            }
        }

        for ( final PackageImport pi : bi.getPackageImports () )
        {
            final Map<String, String> reqs = new HashMap<> ();

            final String filter = and ( //
            pair ( "osgi.wiring.package", pi.getName () ), //
            versionRange ( "version", pi.getVersionRange () ) //
            );

            reqs.put ( "filter", filter );

            if ( pi.isOptional () )
            {
                reqs.put ( "resolution", "optional" );
            }

            addRequirement ( writer, "osgi.wiring.package", reqs );
        }
    }

    private static void addRequirement ( final XMLStreamWriter writer, final String id, final Map<String, String> caps ) throws XMLStreamException
    {
        writer.writeCharacters ( "\t" );
        writer.writeStartElement ( "requirement" );
        writer.writeAttribute ( "namespace", id );
        writer.writeCharacters ( "\n" );

        for ( final Map.Entry<String, String> entry : caps.entrySet () )
        {
            writer.writeCharacters ( "\t\t" );
            writer.writeEmptyElement ( "directive" );
            writer.writeAttribute ( "name", entry.getKey () );
            writer.writeAttribute ( "value", entry.getValue () );

            writer.writeCharacters ( "\n" );
        }

        writer.writeCharacters ( "\t" );
        writer.writeEndElement ();
        writer.writeCharacters ( "\n" );
    }

    private static void addContent ( final XMLStreamWriter writer, final ArtifactInformation a, final String url ) throws XMLStreamException
    {
        final String sha256 = a.getMetaData ().get ( KEY_SHA_256 );

        if ( sha256 == null )
        {
            return;
        }

        final Map<String, Object> caps = new HashMap<> ( 4 );

        caps.put ( "osgi.content", sha256 );
        caps.put ( "size", a.getSize () );
        caps.put ( "mime", "application/vnd.osgi.bundle" );
        caps.put ( "url", url );

        addCapability ( writer, "osgi.content", caps );
    }

    private static void addCapability ( final XMLStreamWriter writer, final String id, final Map<String, Object> caps ) throws XMLStreamException
    {
        writer.writeCharacters ( "\t" );
        writer.writeStartElement ( "capability" );
        writer.writeAttribute ( "namespace", id );
        writer.writeCharacters ( "\n" );

        for ( final Map.Entry<String, Object> entry : caps.entrySet () )
        {
            writer.writeCharacters ( "\t\t" );
            writer.writeEmptyElement ( "attribute" );
            writer.writeAttribute ( "name", entry.getKey () );

            final Object v = entry.getValue ();

            if ( ! ( v instanceof String ) )
            {
                writer.writeAttribute ( "type", v.getClass ().getSimpleName () );
            }

            writer.writeAttribute ( "value", "" + v );

            writer.writeCharacters ( "\n" );
        }

        writer.writeCharacters ( "\t" );
        writer.writeEndElement ();
        writer.writeCharacters ( "\n" );
    }

    public RepositoryCreator ( final String name, final SpoolOutTarget target, final Function<ArtifactInformation, String> urlProvider )
    {
        this.name = name;
        this.urlProvider = urlProvider;

        this.indexStreamBuilder = new OutputSpooler ( target );
        this.indexStreamBuilder.addOutput ( "index.xml", "application/xml" );
    }

    public void process ( final IOConsumer<Context> consumer ) throws IOException
    {
        final XMLOutputFactory xml = XMLOutputFactory.newInstance ();

        this.indexStreamBuilder.open ( stream -> {
            try
            {
                final XMLStreamWriter xsw = xml.createXMLStreamWriter ( stream );
                try
                {
                    xsw.writeStartDocument ();
                    xsw.writeCharacters ( "\n\n" );

                    xsw.writeComment ( String.format ( "Created by Package Drone %s - %tc", VersionInformation.VERSION, new Date () ) );

                    xsw.writeStartElement ( "repository" );
                    xsw.writeDefaultNamespace ( "http://www.osgi.org/xmlns/repository/v1.0.0" );
                    xsw.writeAttribute ( "increment", "" + System.currentTimeMillis () );
                    xsw.writeAttribute ( "name", this.name );

                    xsw.writeCharacters ( "\n\n" );

                    final ContextImpl ctx = new ContextImpl ( xsw, this.urlProvider );
                    consumer.accept ( ctx );

                    xsw.writeEndElement (); // repository
                    xsw.writeEndDocument ();
                }
                finally
                {
                    xsw.close ();
                }
            }
            catch ( final Exception e )
            {
                throw new IOException ( e );
            }
        } );
    }

    private static Version mapFrameworkPackageVersion ( final Version pv )
    {
        if ( pv.getMajor () != 1 )
        {
            return null;
        }

        Version version;
        switch ( pv.getMinor () )
        {
            case 7:
                version = new Version ( 5, 0, 0 );
                break;
            case 6:
                version = new Version ( 4, 3, 0 );
                break;
            case 5:
                version = new Version ( 4, 2, 0 );
                break;
            case 4:
                version = new Version ( 4, 1, 0 );
                break;
            case 3:
                version = new Version ( 4, 0, 0 );
                break;
            case 2:
                version = new Version ( 3, 0, 0 );
                break;
            case 1:
                version = new Version ( 2, 0, 0 );
                break;
            case 0:
                version = new Version ( 1, 0, 0 );
                break;
            default:
                version = null;
                break;
        }

        return version;
    }
}
