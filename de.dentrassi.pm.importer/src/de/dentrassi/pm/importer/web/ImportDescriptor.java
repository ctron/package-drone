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
package de.dentrassi.pm.importer.web;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import com.google.gson.GsonBuilder;

public class ImportDescriptor
{
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
        return new GsonBuilder ().create ().toJson ( this );
    }

    public String toBase64 ()
    {
        return Base64.getEncoder ().encodeToString ( toJson ().getBytes ( StandardCharsets.UTF_8 ) );
    }
}
