/*******************************************************************************
 * Copyright (c) 2014, 2015 Jens Reimann.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jens Reimann - initial API and implementation
 *******************************************************************************/
package de.dentrassi.pm.storage.web.internal.menu;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.dentrassi.osgi.web.ModelAndView;
import de.dentrassi.osgi.web.RequestHandler;
import de.dentrassi.osgi.web.interceptor.ModelAndViewInterceptorAdapter;
import de.dentrassi.pm.storage.web.menu.MenuManager;
import de.dentrassi.pm.storage.web.menu.MenuManagerImpl;

public class MenuInterceptor extends ModelAndViewInterceptorAdapter
{
    private MenuManagerImpl menuManager;

    public void activate ()
    {
        this.menuManager = new MenuManagerImpl ();
    }

    public void deactivate ()
    {
        if ( this.menuManager != null )
        {
            this.menuManager.close ();
            this.menuManager = null;
        }
    }

    @Override
    protected void postHandle ( final HttpServletRequest request, final HttpServletResponse response, final RequestHandler requestHandler, final ModelAndView modelAndView )
    {
        if ( modelAndView != null && !modelAndView.isRedirect () )
        {
            modelAndView.put ( "menuManager", new MenuManager ( this.menuManager, request ) );
        }
    }

}
