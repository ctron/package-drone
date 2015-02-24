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
package de.dentrassi.pm.storage;

public class ChannelLockedException extends IllegalStateException
{
    private static final long serialVersionUID = 1L;

    private final String channelId;

    public ChannelLockedException ( final String channelId )
    {
        super ( String.format ( "Channel '%s' is locked", channelId ) );
        this.channelId = channelId;
    }

    public String getChannelId ()
    {
        return this.channelId;
    }
}
