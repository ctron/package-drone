/*******************************************************************************
 * Copyright (c) 2014, 2015 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package de.dentrassi.pm.storage.web.internal;

import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import de.dentrassi.osgi.web.LinkTarget;
import de.dentrassi.pm.common.web.InterfaceExtender;
import de.dentrassi.pm.common.web.menu.MenuEntry;

public class AboutMenuExtender implements InterfaceExtender
{
    private final List<MenuEntry> entries = new LinkedList<> ();

    public AboutMenuExtender ()
    {
        this.entries.add ( new MenuEntry ( "Help", Integer.MAX_VALUE, "Wiki", 1_000, new LinkTarget ( "https://github.com/ctron/package-drone/wiki" ), null, null, true ) );
        this.entries.add ( new MenuEntry ( "Help", Integer.MAX_VALUE, "Documentation", 1_000, new LinkTarget ( "http://doc.packagedrone.org/book" ), null, null, true ) );
        this.entries.add ( new MenuEntry ( "Help", Integer.MAX_VALUE, "About", Integer.MAX_VALUE, new LinkTarget ( "http://packagedrone.org" ), null, null, true ) );
    }

    @Override
    public List<MenuEntry> getMainMenuEntries ( final HttpServletRequest request )
    {
        return this.entries;
    }
}
