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
package de.dentrassi.pm.core.web;

import org.hibernate.validator.constraints.URL;

import de.dentrassi.pm.common.MetaKeyBinding;

public class SiteInformation
{
    @MetaKeyBinding ( namespace = "core", key = "site-prefix" )
    @URL
    private String prefix;

    public void setPrefix ( final String prefix )
    {
        this.prefix = prefix;
    }

    public String getPrefix ()
    {
        return this.prefix;
    }
}
