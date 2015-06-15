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

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import de.dentrassi.pm.common.web.InterfaceExtender;
import de.dentrassi.pm.common.web.menu.MenuEntry;

/**
 * An abstract InterfaceExtender which only processes Channel instances
 */
public abstract class AbstractChannelInterfaceExtender implements InterfaceExtender
{
    protected boolean filterChannel ( final Channel channel )
    {
        return true;
    }

    @Override
    public List<MenuEntry> getActions ( final HttpServletRequest request, final Object object )
    {
        if ( object instanceof Channel )
        {
            final Channel channel = (Channel)object;
            if ( filterChannel ( channel ) )
            {
                return getChannelActions ( request, channel );
            }
        }
        return null;
    }

    protected List<MenuEntry> getChannelActions ( final HttpServletRequest request, final Channel channel )
    {
        return null;
    }

    @Override
    public List<MenuEntry> getViews ( final HttpServletRequest request, final Object object )
    {
        if ( object instanceof Channel )
        {
            final Channel channel = (Channel)object;
            if ( filterChannel ( channel ) )
            {
                return getChannelViews ( request, channel );
            }
        }
        return null;
    }

    protected List<MenuEntry> getChannelViews ( final HttpServletRequest request, final Channel channel )
    {
        return null;
    }
}
