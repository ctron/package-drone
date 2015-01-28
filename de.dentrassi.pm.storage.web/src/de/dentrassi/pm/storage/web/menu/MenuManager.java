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
package de.dentrassi.pm.storage.web.menu;

import javax.servlet.http.HttpServletRequest;

public class MenuManager
{
    private final HttpServletRequest request;

    private final MenuManagerImpl menuManager;

    public MenuManager ( final MenuManagerImpl menuManager, final HttpServletRequest request )
    {
        this.menuManager = menuManager;
        this.request = request;
    }

    public Menu getMainMenu ()
    {
        return this.menuManager.getMainMenu ( this.request );
    }

    public Menu getActions ( final Object context )
    {
        return this.menuManager.getActions ( this.request, context );
    }

    public Menu getViews ( final Object context )
    {
        return this.menuManager.getViews ( this.request, context );
    }

}
