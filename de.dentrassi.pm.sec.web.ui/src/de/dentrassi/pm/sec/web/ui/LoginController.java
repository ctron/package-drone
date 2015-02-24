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
package de.dentrassi.pm.sec.web.ui;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scada.utils.ExceptionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dentrassi.osgi.web.Controller;
import de.dentrassi.osgi.web.ModelAndView;
import de.dentrassi.osgi.web.RequestMapping;
import de.dentrassi.osgi.web.RequestMethod;
import de.dentrassi.osgi.web.ViewResolver;
import de.dentrassi.osgi.web.controller.form.FormData;
import de.dentrassi.pm.sec.DatabaseDetails;
import de.dentrassi.pm.sec.UserInformationPrincipal;
import de.dentrassi.pm.sec.service.LoginException;
import de.dentrassi.pm.sec.web.filter.SecurityFilter;

@Controller
@ViewResolver ( "/WEB-INF/views/%s.jsp" )
@RequestMapping ( "/login" )
public class LoginController
{
    private final static Logger logger = LoggerFactory.getLogger ( LoginController.class );

    @RequestMapping ( method = RequestMethod.GET )
    public ModelAndView login ( final HttpServletRequest request )
    {
        final Map<String, Object> model = new HashMap<> ();

        final LoginData data = new LoginData ();

        final Cookie[] cookies = request.getCookies ();
        if ( cookies != null )
        {
            for ( final Cookie cookie : cookies )
            {
                if ( cookie.getName ().equals ( SecurityFilter.COOKIE_EMAIL ) )
                {
                    data.setEmail ( cookie.getValue () );
                }
            }
        }

        model.put ( "command", data );

        return new ModelAndView ( "login/form", model );
    }

    @RequestMapping ( method = RequestMethod.POST )
    public ModelAndView loginPost ( @FormData ( "command" ) final LoginData data, final HttpServletRequest request, final HttpServletResponse response )
    {
        try
        {
            request.setAttribute ( SecurityFilter.ATTR_REMEMBER_ME, data.isRememberMeSafe () );

            request.login ( data.getEmail (), data.getPassword () );

            if ( data.isRememberMeSafe () )
            {
                final Principal p = request.getUserPrincipal ();
                if ( p instanceof UserInformationPrincipal )
                {
                    final UserInformationPrincipal uip = (UserInformationPrincipal)p;
                    final DatabaseDetails dd = uip.getUserInformation ().getDetails ( DatabaseDetails.class );

                    if ( dd != null )
                    {
                        final String token = dd.getRememberMeToken ();
                        if ( token != null )
                        {
                            Cookie cookie = new Cookie ( SecurityFilter.COOKIE_REMEMBER_ME, token );
                            cookie.setMaxAge ( (int)TimeUnit.DAYS.toSeconds ( 90 ) );
                            response.addCookie ( cookie );

                            cookie = new Cookie ( SecurityFilter.COOKIE_EMAIL, dd.getEmail () );
                            cookie.setMaxAge ( (int)TimeUnit.DAYS.toSeconds ( 360 ) );
                            response.addCookie ( cookie );
                        }
                    }
                }
            }
        }
        catch ( final ServletException e )
        {
            final Map<String, Object> model = new HashMap<> ();

            final Throwable root = ExceptionHelper.getRootCause ( e );
            if ( root instanceof LoginException )
            {
                model.put ( "errorTitle", root.getMessage () );
                model.put ( "details", ( (LoginException)root ).getDetails () );

                final long failures = Sessions.incrementLoginFailCounter ( request.getSession () );
                model.put ( "failureCount", failures );
            }
            else
            {
                logger.warn ( "Login error", e );
                model.put ( "errorTitle", "Internal error!" );
                model.put ( "details", String.format ( "Failed to log in: %s", root.getClass ().getSimpleName () ) );
            }

            return new ModelAndView ( "login/form", model );
        }

        Sessions.resetLoginFailCounter ( request.getSession () );

        return new ModelAndView ( "redirect:/" );
    }
}
