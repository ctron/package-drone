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
package de.dentrassi.pm.r5.internal;

import java.util.Map;

import de.dentrassi.osgi.xml.XmlToolsFactory;
import de.dentrassi.pm.aspect.aggregate.AggregationContext;
import de.dentrassi.pm.aspect.aggregate.ChannelAggregator;
import de.dentrassi.pm.aspect.common.spool.ChannelCacheTarget;
import de.dentrassi.pm.aspect.common.spool.SpoolOutTarget;
import de.dentrassi.pm.r5.RepositoryCreator;
import de.dentrassi.pm.storage.channel.ArtifactInformation;

public class R5RepoIndexAggregator implements ChannelAggregator
{
    private final XmlToolsFactory xmlFactory;

    public R5RepoIndexAggregator ( final XmlToolsFactory xmlFactory )
    {
        this.xmlFactory = xmlFactory;
    }

    @Override
    public Map<String, String> aggregateMetaData ( final AggregationContext context ) throws Exception
    {
        final String name = context.getChannelId ();

        final SpoolOutTarget target = new ChannelCacheTarget ( context );

        final RepositoryCreator repo = new RepositoryCreator ( name, target, art -> context.getChannelId () + "/artifact/" + art.getId () + "/" + art.getName (), this.xmlFactory::newXMLOutputFactory );

        repo.process ( ctx -> {
            for ( final ArtifactInformation art : context.getArtifacts () )
            {
                ctx.addArtifact ( art );
            }
        } );

        return null;
    }

}
