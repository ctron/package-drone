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
package de.dentrassi.pm.sec.web.ui;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.dentrassi.osgi.web.Controller;
import de.dentrassi.osgi.web.ModelAndView;
import de.dentrassi.osgi.web.RequestMapping;
import de.dentrassi.osgi.web.RequestMethod;
import de.dentrassi.osgi.web.ViewResolver;
import de.dentrassi.pm.sec.web.filter.SecurityFilter;

@Controller
@ViewResolver ( "/WEB-INF/views/%s.jsp" )
@RequestMapping ( "/logout" )
public class LogoutController
{
    @RequestMapping ( method = RequestMethod.GET )
    public ModelAndView logout ( final HttpServletRequest request, final HttpServletResponse response ) throws ServletException
    {
        request.logout ();

        // delete remember me cookie

        final Cookie cookie = new Cookie ( SecurityFilter.COOKIE_REMEMBER_ME, null );
        cookie.setMaxAge ( 0 );
        response.addCookie ( cookie );

        return new ModelAndView ( "redirect:/" );
    }
}
