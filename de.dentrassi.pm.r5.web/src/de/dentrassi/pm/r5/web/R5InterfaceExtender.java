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
package de.dentrassi.pm.r5.web;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import de.dentrassi.osgi.web.LinkTarget;
import de.dentrassi.pm.common.web.Modifier;
import de.dentrassi.pm.common.web.menu.MenuEntry;
import de.dentrassi.pm.r5.R5RepositoryAspectFactory;
import de.dentrassi.pm.storage.AbstractChannelInterfaceExtender;
import de.dentrassi.pm.storage.Channel;

public class R5InterfaceExtender extends AbstractChannelInterfaceExtender
{
    @Override
    protected List<MenuEntry> getChannelActions ( final HttpServletRequest request, final Channel channel )
    {
        if ( !channel.hasAspect ( R5RepositoryAspectFactory.ID ) )
        {
            return null;
        }

        final Map<String, String> model = new HashMap<> ();
        model.put ( "channelId", channel.getId () );
        if ( channel.getName () != null && !channel.getName ().isEmpty () )
        {
            model.put ( "channelAlias", channel.getName () );
        }

        final List<MenuEntry> result = new LinkedList<> ();

        result.add ( new MenuEntry ( "R5 (by ID)", 10_000, new LinkTarget ( "/r5/{channelId}" ).expand ( model ), Modifier.LINK, null ) );
        if ( model.containsKey ( "channelAlias" ) )
        {
            result.add ( new MenuEntry ( "R5 (by name)", 10_000, new LinkTarget ( "/r5/{channelAlias}" ).expand ( model ), Modifier.LINK, null ) );
        }

        return result;
    }
}
