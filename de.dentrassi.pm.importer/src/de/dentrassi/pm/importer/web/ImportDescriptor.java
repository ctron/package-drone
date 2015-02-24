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
package de.dentrassi.pm.importer.web;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import com.google.gson.GsonBuilder;

public class ImportDescriptor
{
    private static GsonBuilder BUILDER = new GsonBuilder ();

    private String type;

    private String id;

    public void setId ( final String id )
    {
        this.id = id;
    }

    public String getId ()
    {
        return this.id;
    }

    public void setType ( final String type )
    {
        this.type = type;
    }

    public String getType ()
    {
        return this.type;
    }

    public String toJson ()
    {
        return BUILDER.create ().toJson ( this );
    }

    public String toBase64 ()
    {
        return Base64.getEncoder ().encodeToString ( toJson ().getBytes ( StandardCharsets.UTF_8 ) );
    }

    public static ImportDescriptor fromBase64 ( final String token )
    {
        if ( token == null )
        {
            return null;
        }

        final String str = StandardCharsets.UTF_8.decode ( ByteBuffer.wrap ( Base64.getDecoder ().decode ( token ) ) ).toString ();

        return fromJson ( str );
    }

    public static ImportDescriptor fromJson ( final String json )
    {
        if ( json == null )
        {
            return null;
        }
        return BUILDER.create ().fromJson ( json, ImportDescriptor.class );
    }
}
