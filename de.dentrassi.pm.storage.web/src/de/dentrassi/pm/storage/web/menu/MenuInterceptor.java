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
package de.dentrassi.pm.storage.web.menu;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.dentrassi.osgi.web.ModelAndView;
import de.dentrassi.osgi.web.RequestHandler;
import de.dentrassi.osgi.web.interceptor.ModelAndViewInterceptorAdapter;

public class MenuInterceptor extends ModelAndViewInterceptorAdapter
{
    private MenuManager menuManager;

    public void activate ()
    {
        this.menuManager = new MenuManager ();
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
            modelAndView.put ( "currentUrl", request.getServletPath () );
            modelAndView.put ( "menuManager", this.menuManager );
        }
    }

}
