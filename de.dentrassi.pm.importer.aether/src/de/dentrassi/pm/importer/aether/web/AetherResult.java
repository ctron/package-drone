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
package de.dentrassi.pm.importer.aether.web;

import com.google.gson.GsonBuilder;

import de.dentrassi.osgi.converter.JSON;
import de.dentrassi.pm.importer.aether.MavenCoordinates;

@JSON
public class AetherResult
{
    private static final GsonBuilder BUILDER = new GsonBuilder ();

    private boolean resolved;

    private MavenCoordinates coordinates;

    private String url;

    public String toJson ()
    {
        return BUILDER.create ().toJson ( this );
    }

    public static AetherResult fromJson ( final String json )
    {
        return BUILDER.create ().fromJson ( json, AetherResult.class );
    }

    public void setResolved ( final boolean resolved )
    {
        this.resolved = resolved;
    }

    public boolean isResolved ()
    {
        return this.resolved;
    }

    public String getUrl ()
    {
        return this.url;
    }

    public void setUrl ( final String url )
    {
        this.url = url;
    }

    public void setCoordinates ( final MavenCoordinates coordinates )
    {
        this.coordinates = coordinates;
    }

    public MavenCoordinates getCoordinates ()
    {
        return this.coordinates;
    }
}
