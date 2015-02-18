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
package de.dentrassi.pm.importer.http.web;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.HttpConstraint;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import de.dentrassi.osgi.job.JobHandle;
import de.dentrassi.osgi.job.web.Jobs;
import de.dentrassi.osgi.web.Controller;
import de.dentrassi.osgi.web.ModelAndView;
import de.dentrassi.osgi.web.RequestMapping;
import de.dentrassi.osgi.web.RequestMethod;
import de.dentrassi.osgi.web.ViewResolver;
import de.dentrassi.osgi.web.controller.ControllerInterceptor;
import de.dentrassi.osgi.web.controller.binding.BindingResult;
import de.dentrassi.osgi.web.controller.form.FormData;
import de.dentrassi.pm.importer.http.Configuration;
import de.dentrassi.pm.sec.web.controller.HttpContraintControllerInterceptor;
import de.dentrassi.pm.sec.web.controller.Secured;
import de.dentrassi.pm.sec.web.controller.SecuredControllerInterceptor;

@Secured
@Controller
@ViewResolver ( "/WEB-INF/views/%s.jsp" )
@ControllerInterceptor ( SecuredControllerInterceptor.class )
@HttpConstraint ( rolesAllowed = "MANAGER" )
@ControllerInterceptor ( HttpContraintControllerInterceptor.class )
public class ConfigurationController
{
    @RequestMapping ( value = "/import/{token}/http/start", method = RequestMethod.GET )
    public ModelAndView configure ()
    {
        final Map<String, Object> model = new HashMap<> ();

        model.put ( "command", new Configuration () );

        return new ModelAndView ( "configure", model );
    }

    @RequestMapping ( value = "/import/{token}/http/start", method = RequestMethod.POST )
    public ModelAndView configurePost ( @Valid @FormData ( "command" ) final Configuration data, final BindingResult result, final HttpServletRequest request, final HttpServletResponse response ) throws ServletException, IOException
    {
        final Map<String, Object> model = new HashMap<> ();

        if ( !result.hasErrors () )
        {
            final RequestDispatcher rd = request.getRequestDispatcher ( "test" );
            rd.forward ( request, response );
            return null;
        }
        else
        {
            return new ModelAndView ( "configure", model );
        }
    }

    @RequestMapping ( value = "/import/{token}/http/test", method = RequestMethod.POST )
    public ModelAndView testImport ( @Valid @FormData ( "command" ) final Configuration data, final BindingResult result, final HttpServletRequest request )
    {
        final Map<String, Object> model = new HashMap<> ();

        model.put ( "command", data );

        final JobHandle job = Jobs.start ( request, "Test Import", new Callable<Void> () {

            @Override
            public Void call () throws Exception
            {
                System.out.println ( "Job started" );
                Thread.sleep ( 5_000 );
                System.out.println ( "Job ending" );

                final HttpURLConnection con = (HttpURLConnection)new URL ( data.getUrl () ).openConnection ();

                con.setRequestMethod ( "HEAD" );

                final boolean result = con.getResponseCode () == HttpURLConnection.HTTP_OK;

                // throw new RuntimeException ();
                return null;
            }
        } );

        final HttpSession session = request.getSession ();
        session.setAttribute ( ConfigurationController.class.getName () + "-" + job.getId (), job );

        model.put ( "job", job );

        return new ModelAndView ( "test", model );
    }

    @RequestMapping ( value = "/import/{token}/http/test", method = RequestMethod.POST )
    public ModelAndView completeTest ( @Valid @FormData ( "command" ) final Configuration data, final BindingResult result, final HttpServletRequest request )
    {
        final Map<String, Object> model = new HashMap<> ();

        model.put ( "command", data );

        final JobHandle job = Jobs.start ( request, "Test Import", new Callable<Void> () {

            @Override
            public Void call () throws Exception
            {
                System.out.println ( "Job started" );
                Thread.sleep ( 5_000 );
                System.out.println ( "Job ending" );

                final HttpURLConnection con = (HttpURLConnection)new URL ( data.getUrl () ).openConnection ();

                con.setRequestMethod ( "HEAD" );

                final boolean result = con.getResponseCode () == HttpURLConnection.HTTP_OK;

                // throw new RuntimeException ();
                return null;
            }
        } );

        final HttpSession session = request.getSession ();
        session.setAttribute ( ConfigurationController.class.getName () + "-" + job.getId (), job );

        model.put ( "job", job );

        return new ModelAndView ( "test", model );
    }
}
