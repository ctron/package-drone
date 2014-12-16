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
package de.dentrassi.pm.generator.p2;

import javax.validation.constraints.Pattern;

public class CreateData
{

    @Pattern ( regexp = "[a-z0-9]+(\\.[a-z0-9]+)*", message = "Must be a valid feature ID" )
    private String id;

    @Pattern ( regexp = "[0-9]+((\\.[0-9]+){1,2}(\\.[^\\.])?)",
            message = "Must be a valid version string: major.minor[.micro[.qualifier]]" )
    private String version;

    public void setId ( final String id )
    {
        this.id = id;
    }

    public String getId ()
    {
        return this.id;
    }

    public void setVersion ( final String version )
    {
        this.version = version;
    }

    public String getVersion ()
    {
        return this.version;
    }

}
