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
package de.dentrassi.pm.storage.web.channel;

import java.util.LinkedList;
import java.util.List;

import de.dentrassi.pm.storage.service.StorageService;
import de.dentrassi.pm.storage.web.menu.MenuExtender;
import de.dentrassi.pm.storage.web.menu.MenuManager.MenuEntry;

public class ChannelMenuExtender implements MenuExtender
{
    private final List<MenuEntry> entries = new LinkedList<> ();

    public void setService ( final StorageService service )
    {
        // dummy method
    }

    public ChannelMenuExtender ()
    {
        this.entries.add ( new MenuEntry ( "/channel", "Channels", 10 ) );
    }

    @Override
    public List<MenuEntry> getEntries ()
    {
        return this.entries;
    }

}
