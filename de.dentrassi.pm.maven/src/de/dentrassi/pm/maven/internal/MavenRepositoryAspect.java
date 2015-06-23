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
import de.dentrassi.pm.aspect.aggregate.ChannelAggregator;
import de.dentrassi.pm.system.SitePrefixService;

public class MavenRepositoryAspect implements ChannelAspect
{
    private final SitePrefixService sitePrefixService;

    public MavenRepositoryAspect ( final SitePrefixService sitePrefixService )
    {
        this.sitePrefixService = sitePrefixService;
    }

    @Override
    public String getId ()
    {
        return MavenRepositoryAspectFactory.ID;
    }

    @Override
    public ChannelAggregator getChannelAggregator ()
    {
        return new MavenRepositoryChannelAggregator ( this.sitePrefixService );
    }
}
