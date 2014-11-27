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
package de.dentrassi.pm.osgi;

import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.osgi.framework.Constants;

public class BundleInformationParser
{
    private final Manifest manifest;

    public BundleInformationParser ( final Manifest manifest )
    {
        this.manifest = manifest;
    }

    public BundleInformation parse () throws IOException
    {
        final BundleInformation result = new BundleInformation ();

        final Attributes ma = this.manifest.getMainAttributes ();

        final AttributedValue id = Headers.parse ( ma.getValue ( Constants.BUNDLE_SYMBOLICNAME ) );
        final AttributedValue version = Headers.parse ( ma.getValue ( Constants.BUNDLE_VERSION ) );
        if ( id == null )
        {
            return null;
        }

        result.setId ( id.getValue () );
        result.setVersion ( version.getValue () );
        result.setName ( ma.getValue ( Constants.BUNDLE_NAME ) );

        return result;
    }
}
