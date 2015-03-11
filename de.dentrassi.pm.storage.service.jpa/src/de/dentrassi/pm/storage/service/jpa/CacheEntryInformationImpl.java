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
package de.dentrassi.pm.storage.service.jpa;

import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.storage.CacheEntryInformation;

public class CacheEntryInformationImpl implements CacheEntryInformation
{
    private final MetaKey key;

    private final String name;

    private final long size;

    private final String mimeType;

    public CacheEntryInformationImpl ( final MetaKey key, final String name, final long size, final String mimeType )
    {
        this.key = key;
        this.name = name;
        this.size = size;
        this.mimeType = mimeType;
    }

    @Override
    public long getSize ()
    {
        return this.size;
    }

    @Override
    public String getName ()
    {
        return this.name;
    }

    @Override
    public String getMimeType ()
    {
        return this.mimeType;
    }

    @Override
    public MetaKey getKey ()
    {
        return this.key;
    }
}
