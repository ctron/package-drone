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
package de.dentrassi.pm.utils.deb.internal;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

import org.vafer.jdeb.debian.ControlField;
import org.vafer.jdeb.debian.ControlFile;

public final class BinarySectionPackagesFile extends ControlFile
{
    private static final ControlField[] FIELDS = { //
    new ControlField ( "Package", true ), //
    new ControlField ( "Source" ), //
    new ControlField ( "Version", true ), //
    new ControlField ( "Section", true ), //
    new ControlField ( "Priority", true ), //
    new ControlField ( "Architecture", true ), //
    new ControlField ( "Essential" ), //
    new ControlField ( "Depends" ), //
    new ControlField ( "Pre-Depends" ),//
    new ControlField ( "Recommends" ),//
    new ControlField ( "Suggests" ), //
    new ControlField ( "Breaks" ), //
    new ControlField ( "Enhances" ), //
    new ControlField ( "Conflicts" ), //
    new ControlField ( "Provides" ), //
    new ControlField ( "Replaces" ), //
    new ControlField ( "Installed-Size" ), //
    new ControlField ( "Maintainer", true ), //
    new ControlField ( "Description", true, ControlField.Type.MULTILINE ),//
    new ControlField ( "Description-md5" ),//
    new ControlField ( "Homepage" ), //
    new ControlField ( "Installed-Size" ), //
    new ControlField ( "SHA256" ), //
    new ControlField ( "SHA1" ), //
    new ControlField ( "MD5sum" ), //
    new ControlField ( "Size", true ), //
    new ControlField ( "Filename" ) //
    };

    public BinarySectionPackagesFile ()
    {
        set ( "Architecture", "all" );
        set ( "Priority", "optional" );
    }

    public BinarySectionPackagesFile ( final String input ) throws IOException, ParseException
    {
        parse ( input );
    }

    public BinarySectionPackagesFile ( final InputStream input ) throws IOException, ParseException
    {
        parse ( input );
    }

    @Override
    protected ControlField[] getFields ()
    {
        return FIELDS;
    }

    public String getShortDescription ()
    {
        final String desc = get ( "Description" );
        if ( desc == null )
        {
            return null;
        }
        return desc.split ( "\n" )[0];
    }

    @Override
    protected char getUserDefinedFieldLetter ()
    {
        return 'B';
    }
}
