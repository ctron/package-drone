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

import de.dentrassi.pm.storage.channel.ChannelId;
import de.dentrassi.pm.storage.channel.DescriptorAdapter;

class DescriptorAdapterImpl implements DescriptorAdapter
{
    private ChannelId descriptor;

    public DescriptorAdapterImpl ( final ChannelEntry channel )
    {
        this.descriptor = channel.getId ();
    }

    @Override
    public void setName ( final String name )
    {
        this.descriptor = new ChannelId ( this.descriptor.getId (), name );
    }

    @Override
    public ChannelId getDescriptor ()
    {
        return this.descriptor;
    }
}
