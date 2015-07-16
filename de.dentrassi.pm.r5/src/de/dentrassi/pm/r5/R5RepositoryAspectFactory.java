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
package de.dentrassi.pm.r5;

import de.dentrassi.osgi.xml.XmlToolsFactory;
import de.dentrassi.pm.aspect.ChannelAspect;
import de.dentrassi.pm.aspect.ChannelAspectFactory;
import de.dentrassi.pm.aspect.aggregate.ChannelAggregator;
import de.dentrassi.pm.r5.internal.R5RepoIndexAggregator;

public class R5RepositoryAspectFactory implements ChannelAspectFactory
{
    public static final String ID = "r5.repo";

    private XmlToolsFactory xmlFactory;

    public void setXmlFactory ( final XmlToolsFactory xmlFactory )
    {
        this.xmlFactory = xmlFactory;
    }

    @Override
    public ChannelAspect createAspect ()
    {
        return new ChannelAspect () {

            @Override
            public String getId ()
            {
                return ID;
            }

            @Override
            public ChannelAggregator getChannelAggregator ()
            {
                return new R5RepoIndexAggregator ( R5RepositoryAspectFactory.this.xmlFactory );
            }
        };
    }

}
