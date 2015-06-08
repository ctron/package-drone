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
package de.dentrassi.pm.p2.web;

import de.dentrassi.pm.common.MetaKeyBinding;

public class P2MetaDataInformation
{
    @MetaKeyBinding ( namespace = P2InterfaceExtender.P2_METADATA_ASPECT_ID, key = "system-bundle-alias" )
    private String systemBundleAlias;

    public void setSystemBundleAlias ( final String systemBundleAlias )
    {
        this.systemBundleAlias = systemBundleAlias;
    }

    public String getSystemBundleAlias ()
    {
        return this.systemBundleAlias;
    }
}
