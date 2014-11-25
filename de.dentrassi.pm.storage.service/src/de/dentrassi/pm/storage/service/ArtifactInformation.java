/*******************************************************************************
 * Copyright (c) 2014 Jens Reimann.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jens Reimann - initial API and implementation
 *******************************************************************************/
package de.dentrassi.pm.storage.service;

public class ArtifactInformation
{
    private final long length;

    private final String name;

    private final String channelId;

    public ArtifactInformation ( final long size, final String name, final String channelId )
    {
        this.length = size;
        this.name = name;
        this.channelId = channelId;
    }

    public long getLength ()
    {
        return this.length;
    }

    public String getName ()
    {
        return this.name;
    }

    public String getChannelId ()
    {
        return this.channelId;
    }
}
