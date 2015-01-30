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
package de.dentrassi.pm.osgi.bundle;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.osgi.framework.Constants;
import org.osgi.framework.Version;
import org.osgi.framework.VersionRange;

import de.dentrassi.osgi.utils.AttributedValue;
import de.dentrassi.osgi.utils.Headers;
import de.dentrassi.pm.osgi.ParserHelper;
import de.dentrassi.pm.osgi.bundle.BundleInformation.BundleRequirement;
import de.dentrassi.pm.osgi.bundle.BundleInformation.PackageExport;
import de.dentrassi.pm.osgi.bundle.BundleInformation.PackageImport;

public class BundleInformationParser
{
    private final ZipFile file;

    public BundleInformationParser ( final ZipFile file )
    {
        this.file = file;
    }

    public BundleInformation parse () throws IOException
    {
        final BundleInformation result = new BundleInformation ();

        final Manifest m = getManifest ();
        if ( m == null )
        {
            return null;
        }

        final Attributes ma = m.getMainAttributes ();

        final AttributedValue id = Headers.parse ( ma.getValue ( Constants.BUNDLE_SYMBOLICNAME ) );
        final AttributedValue version = Headers.parse ( ma.getValue ( Constants.BUNDLE_VERSION ) );
        if ( id == null )
        {
            return null;
        }

        result.setId ( id.getValue () );
        result.setSingleton ( Boolean.parseBoolean ( id.getAttributes ().get ( "singleton" ) ) );
        result.setVersion ( version.getValue () );

        result.setName ( ma.getValue ( Constants.BUNDLE_NAME ) );
        result.setVendor ( ma.getValue ( Constants.BUNDLE_VENDOR ) );

        result.setDocUrl ( ma.getValue ( Constants.BUNDLE_DOCURL ) );
        result.setLicense ( ma.getValue ( Constants.BUNDLE_LICENSE ) );
        result.setDescription ( ma.getValue ( Constants.BUNDLE_DESCRIPTION ) );

        result.setEclipseBundleShape ( ma.getValue ( "Eclipse-BundleShape" ) );

        result.setRequiredExecutionEnvironments ( Headers.parseStringList ( ma.getValue ( Constants.BUNDLE_REQUIREDEXECUTIONENVIRONMENT ) ) );

        processImportPackage ( result, ma );
        processExportPackage ( result, ma );
        processImportBundle ( result, ma );

        attachLocalization ( result, ma );

        return result;
    }

    private void processImportBundle ( final BundleInformation result, final Attributes ma )
    {
        for ( final AttributedValue av : emptyNull ( Headers.parseList ( ma.getValue ( Constants.REQUIRE_BUNDLE ) ) ) )
        {
            final String name = av.getValue ();
            final String vs = av.getAttributes ().get ( "version" );
            VersionRange vr = null;
            if ( vs != null )
            {
                vr = new VersionRange ( vs );
            }
            final boolean optional = "optional".equals ( av.getAttributes ().get ( "resolution" ) );
            final boolean reexport = "reexport".equals ( av.getAttributes ().get ( "visibility" ) );
            result.getBundleRequirements ().add ( new BundleRequirement ( name, vr, optional, reexport ) );
        }
    }

    private void processImportPackage ( final BundleInformation result, final Attributes ma )
    {
        for ( final AttributedValue av : emptyNull ( Headers.parseList ( ma.getValue ( Constants.IMPORT_PACKAGE ) ) ) )
        {
            final String name = av.getValue ();
            final String vs = av.getAttributes ().get ( "version" );
            VersionRange vr = null;
            if ( vs != null )
            {
                vr = new VersionRange ( vs );
            }
            final boolean optional = "optional".equals ( av.getAttributes ().get ( "resolution" ) );
            result.getPackageImports ().add ( new PackageImport ( name, vr, optional ) );
        }
    }

    private void processExportPackage ( final BundleInformation result, final Attributes ma )
    {
        for ( final AttributedValue av : emptyNull ( Headers.parseList ( ma.getValue ( Constants.EXPORT_PACKAGE ) ) ) )
        {
            final String name = av.getValue ();
            final String vs = av.getAttributes ().get ( "version" );
            Version v = null;
            if ( vs != null )
            {
                v = new Version ( vs );
            }
            result.getPackageExports ().add ( new PackageExport ( name, v ) );
        }
    }

    private <T> Collection<T> emptyNull ( final Collection<T> list )
    {
        if ( list == null )
        {
            return Collections.emptyList ();
        }

        return list;
    }

    private void attachLocalization ( final BundleInformation result, final Attributes ma ) throws IOException
    {
        final String loc = ma.getValue ( Constants.BUNDLE_LOCALIZATION );
        if ( loc == null )
        {
            return;
        }

        result.setBundleLocalization ( loc );
        result.setLocalization ( ParserHelper.loadLocalization ( this.file, loc ) );
    }

    private Manifest getManifest () throws IOException
    {
        final ZipEntry m = this.file.getEntry ( "META-INF/MANIFEST.MF" );
        if ( m == null )
        {
            return null;
        }
        try ( InputStream is = this.file.getInputStream ( m ) )
        {
            return new Manifest ( is );
        }
    }
}
