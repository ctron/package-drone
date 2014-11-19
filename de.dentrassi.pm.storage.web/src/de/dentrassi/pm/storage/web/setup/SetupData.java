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
package de.dentrassi.pm.storage.web.setup;

import org.hibernate.validator.constraints.NotEmpty;

public class SetupData
{
    @NotEmpty
    private String jdbcDriver;

    @NotEmpty
    private String url;

    private String user;

    private String password;

    private String additionalProperties;

    public String getJdbcDriver ()
    {
        return this.jdbcDriver;
    }

    public void setJdbcDriver ( final String jdbcDriver )
    {
        this.jdbcDriver = jdbcDriver;
    }

    public String getUrl ()
    {
        return this.url;
    }

    public void setUrl ( final String url )
    {
        this.url = url;
    }

    public String getUser ()
    {
        return this.user;
    }

    public void setUser ( final String user )
    {
        this.user = user;
    }

    public String getPassword ()
    {
        return this.password;
    }

    public void setPassword ( final String password )
    {
        this.password = password;
    }

    public String getAdditionalProperties ()
    {
        return this.additionalProperties;
    }

    public void setAdditionalProperties ( final String additionalProperties )
    {
        this.additionalProperties = additionalProperties;
    }

}
