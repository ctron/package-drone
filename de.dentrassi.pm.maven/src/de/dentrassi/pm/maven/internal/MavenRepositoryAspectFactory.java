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
package de.dentrassi.pm.maven.internal;

import de.dentrassi.pm.aspect.ChannelAspect;
import de.dentrassi.pm.aspect.ChannelAspectFactory;
import de.dentrassi.pm.core.CoreService;
import de.dentrassi.pm.system.SystemService;

public class MavenRepositoryAspectFactory implements ChannelAspectFactory
{
    public static final String ID = "maven.repo";

    private CoreService coreService;

    private SystemService systemService;

    public void setCoreService ( final CoreService coreService )
    {
        this.coreService = coreService;
    }

    public void setSystemService ( final SystemService systemService )
    {
        this.systemService = systemService;
    }

    @Override
    public ChannelAspect createAspect ()
    {
        return new MavenRepositoryAspect ( this.coreService, this.systemService );
    }

}
