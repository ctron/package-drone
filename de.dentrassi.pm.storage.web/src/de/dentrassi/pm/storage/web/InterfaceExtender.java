/*******************************************************************************
 * Copyright (c) 2015 Jens Reimann.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jens Reimann - initial API and implementation
 *******************************************************************************/
package de.dentrassi.pm.storage.web;

import java.util.List;

import de.dentrassi.pm.storage.web.menu.MenuEntry;

public interface InterfaceExtender
{
    public default List<MenuEntry> getActions ( final Object object )
    {
        return null;
    }

    public default List<MenuEntry> getViews ( final Object object )
    {
        return null;
    }

    public default List<MenuEntry> getMainMenuEntries ()
    {
        return null;
    }
}
