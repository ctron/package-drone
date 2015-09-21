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
package de.dentrassi.pm.setup.web;

public class StorageConfiguration
{
    private String basePath;

    public void setBasePath ( final String baseLocation )
    {
        this.basePath = baseLocation;
    }

    public String getBasePath ()
    {
        return this.basePath;
    }
}
