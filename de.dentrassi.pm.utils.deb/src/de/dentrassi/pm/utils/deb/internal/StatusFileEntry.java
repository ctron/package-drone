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
package de.dentrassi.pm.utils.deb.internal;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

import org.vafer.jdeb.debian.ControlField;
import org.vafer.jdeb.debian.ControlFile;

public final class StatusFileEntry extends ControlFile
{
    private static final ControlField[] FIELDS = { //
            new ControlField ( "Package", true ), //
            new ControlField ( "Status", true ), //
            new ControlField ( "Priority", true ), //
            new ControlField ( "Section", false ), //
            new ControlField ( "Installed-Size", true ), //
            new ControlField ( "Maintainer", false ), //
            new ControlField ( "Architecture", true ), //
            new ControlField ( "Multi-Arch", false ), //
            new ControlField ( "Source", false ), //
            new ControlField ( "Version", true ), //

            new ControlField ( "Replaces", false ), //
            new ControlField ( "Pre-Depends", false ), //
            new ControlField ( "Depends", false ), //
            new ControlField ( "Recommends", false ), //

            new ControlField ( "Description", true, ControlField.Type.MULTILINE ), //

            new ControlField ( "Homepage", false ), //
            new ControlField ( "Original-Maintainer", false ), //
    };

    public StatusFileEntry ()
    {
        set ( "Architecture", "all" );
        set ( "Priority", "optional" );
    }

    public StatusFileEntry ( final String input ) throws IOException, ParseException
    {
        parse ( input );
    }

    public StatusFileEntry ( final InputStream input ) throws IOException, ParseException
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
