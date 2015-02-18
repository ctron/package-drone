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
package de.dentrassi.pm.importer.http;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.URL;

import de.dentrassi.osgi.converter.JSON;

@JSON
public class Configuration
{
    @URL
    @NotEmpty
    @NotNull
    private String url;

    public void setUrl ( final String url )
    {
        this.url = url;
    }

    public String getUrl ()
    {
        return this.url;
    }

    private String alternateName;

    public void setAlternateName ( final String alternateName )
    {
        this.alternateName = alternateName;
    }

    public String getAlternateName ()
    {
        return this.alternateName;
    }
}
