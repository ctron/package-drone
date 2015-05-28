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
package de.dentrassi.pm.rpm.yum.internal;

import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;

import de.dentrassi.osgi.web.LinkTarget;
import de.dentrassi.pm.common.web.InterfaceExtender;
import de.dentrassi.pm.common.web.Modifier;
import de.dentrassi.pm.common.web.menu.MenuEntry;
import de.dentrassi.pm.rpm.Constants;
import de.dentrassi.pm.storage.Channel;

public class YumInterfaceExtender implements InterfaceExtender
{
    private static final Escaper PATH_ESC = UrlEscapers.urlPathSegmentEscaper ();

    @Override
    public List<MenuEntry> getActions ( final HttpServletRequest request, final Object object )
    {
        if ( ! ( object instanceof Channel ) )
        {
            return null;
        }

        final Channel channel = (Channel)object;

        if ( !channel.hasAspect ( Constants.YUM_ASPECT_ID ) )
        {
            return null;
        }

        final List<MenuEntry> result = new LinkedList<> ();
        result.add ( new MenuEntry ( "YUM (by ID)", 6_000, new LinkTarget ( String.format ( "/yum/%s", channel.getId () ) ), Modifier.LINK, null ) );
        if ( channel.getName () != null )
        {
            result.add ( new MenuEntry ( "YUM (by name)", 6_000, new LinkTarget ( String.format ( "/yum/%s", PATH_ESC.escape ( channel.getName () ) ) ), Modifier.LINK, null ) );
        }
        return result;
    }
}
