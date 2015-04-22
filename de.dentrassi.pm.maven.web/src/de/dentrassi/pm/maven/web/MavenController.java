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
package de.dentrassi.pm.maven.web;

import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import de.dentrassi.osgi.web.LinkTarget;
import de.dentrassi.pm.common.web.InterfaceExtender;
import de.dentrassi.pm.common.web.Modifier;
import de.dentrassi.pm.common.web.menu.MenuEntry;
import de.dentrassi.pm.storage.Channel;

public class MavenController implements InterfaceExtender
{
    @Override
    public List<MenuEntry> getActions ( final HttpServletRequest request, final Object object )
    {
        if ( object instanceof Channel )
        {
            final Channel channel = (Channel)object;
            if ( channel.hasAspect ( "maven.repo" ) )
            {
                final List<MenuEntry> result = new LinkedList<> ();

                result.add ( new MenuEntry ( "Maven Repository", 20_000, new LinkTarget ( "/maven/" + channel.getId () ), Modifier.LINK, null ) );

                return result;
            }
        }

        return null;
    }
}
