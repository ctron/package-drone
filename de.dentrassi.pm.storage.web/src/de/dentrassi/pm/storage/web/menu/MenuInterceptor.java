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

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import de.dentrassi.pm.storage.web.InterceptorHelper;

public class MenuInterceptor extends HandlerInterceptorAdapter
{
    private final MenuManager menuManager = new MenuManager ();

    @Override
    public void postHandle ( final HttpServletRequest request, final HttpServletResponse response, final Object handler, final ModelAndView modelAndView ) throws Exception
    {
        if ( modelAndView != null && !modelAndView.getViewName ().startsWith ( "redirect:" ) )
        {
            modelAndView.addObject ( "currentUrl", InterceptorHelper.makeCurrent ( request ) );
            modelAndView.addObject ( "menuManager", this.menuManager );
        }

        super.postHandle ( request, response, handler, modelAndView );
    }

}
