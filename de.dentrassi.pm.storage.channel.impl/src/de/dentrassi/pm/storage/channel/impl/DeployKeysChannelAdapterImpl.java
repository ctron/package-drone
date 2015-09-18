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
package de.dentrassi.pm.storage.channel.impl;

import java.util.Set;

import de.dentrassi.pm.storage.channel.DeployKeysChannelAdapter;
import de.dentrassi.pm.storage.channel.deploy.DeployGroup;

public class DeployKeysChannelAdapterImpl implements DeployKeysChannelAdapter
{
    protected final String channelId;

    protected final ChannelServiceModify model;

    public DeployKeysChannelAdapterImpl ( final String channelId, final ChannelServiceModify model )
    {
        this.channelId = channelId;
        this.model = model;
    }

    @Override
    public Set<DeployGroup> getDeployGroups ()
    {
        return this.model.getDeployGroupsForChannel ( this.channelId );
    }

    @Override
    public void assignDeployGroup ( final String groupId )
    {
        this.model.assignDeployGroup ( this.channelId, groupId );
    }

    @Override
    public void unassignDeployGroup ( final String groupId )
    {
        this.model.unassignDeployGroup ( this.channelId, groupId );
    }

}
