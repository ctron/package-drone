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
package de.dentrassi.pm.common.web;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import de.dentrassi.pm.common.web.menu.MenuEntry;

public interface InterfaceExtender
{
    public default List<MenuEntry> getActions ( final HttpServletRequest request, final Object object )
    {
        return null;
    }

    public default List<MenuEntry> getViews ( final HttpServletRequest request, final Object object )
    {
        return null;
    }

    public default List<MenuEntry> getMainMenuEntries ( final HttpServletRequest request )
    {
        return null;
    }
}
