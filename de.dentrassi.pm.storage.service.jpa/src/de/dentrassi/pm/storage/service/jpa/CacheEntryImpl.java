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

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.storage.CacheEntry;
import de.dentrassi.pm.storage.jpa.ChannelCacheEntity;

public class CacheEntryImpl implements CacheEntry
{
    private final ByteArrayInputStream stream;

    private final ChannelCacheEntity cce;

    public CacheEntryImpl ( final ByteArrayInputStream stream, final ChannelCacheEntity cce )
    {
        this.stream = stream;
        this.cce = cce;
    }

    @Override
    public MetaKey getKey ()
    {
        return new MetaKey ( this.cce.getNamespace (), this.cce.getKey () );
    }

    @Override
    public InputStream getStream ()
    {
        return this.stream;
    }

    @Override
    public String getName ()
    {
        return this.cce.getName ();
    }

    @Override
    public String getMimeType ()
    {
        return this.cce.getMimeType ();
    }

    @Override
    public long getSize ()
    {
        return this.cce.getSize ();
    }
}
