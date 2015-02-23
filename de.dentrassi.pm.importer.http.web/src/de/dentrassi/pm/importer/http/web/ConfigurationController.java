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

import java.util.HashMap;
import java.util.Map;

import javax.servlet.annotation.HttpConstraint;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.dentrassi.osgi.job.JobHandle;
import de.dentrassi.osgi.job.JobManager;
import de.dentrassi.osgi.web.Controller;
import de.dentrassi.osgi.web.ModelAndView;
import de.dentrassi.osgi.web.RequestMapping;
import de.dentrassi.osgi.web.RequestMethod;
import de.dentrassi.osgi.web.ViewResolver;
import de.dentrassi.osgi.web.controller.ControllerInterceptor;
import de.dentrassi.osgi.web.controller.binding.BindingResult;
import de.dentrassi.osgi.web.controller.binding.PathVariable;
import de.dentrassi.osgi.web.controller.binding.RequestParameter;
import de.dentrassi.osgi.web.controller.form.FormData;
import de.dentrassi.osgi.web.controller.validator.ControllerValidator;
import de.dentrassi.osgi.web.controller.validator.ValidationContext;
import de.dentrassi.pm.importer.http.Configuration;
import de.dentrassi.pm.importer.http.HttpImporter;
import de.dentrassi.pm.importer.web.ImportRequest;
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

    private final Gson gson = new GsonBuilder ().create ();

    private JobManager jobManager;

    public void setJobManager ( final JobManager jobManager )
    {
        this.jobManager = jobManager;
    }

    @RequestMapping ( value = "/import/{token}/http/start", method = RequestMethod.GET )
    public ModelAndView configure ( @RequestParameter ( value = "configuration", required = false ) final Configuration cfg )
    {
        final Map<String, Object> model = new HashMap<> ();

        if ( cfg != null )
        {
            model.put ( "command", cfg );
        }
        else
        {
            model.put ( "command", new Configuration () );
        }

        return new ModelAndView ( "configure", model );
    }

    @RequestMapping ( value = "/import/{token}/http/start", method = RequestMethod.POST )
    public ModelAndView configurePost ( @Valid @FormData ( "command" ) final Configuration data, final BindingResult result )
    {
        final Map<String, Object> model = new HashMap<> ();

        model.put ( "ok", !result.hasErrors () );

        return new ModelAndView ( "configure", model );
    }

    @RequestMapping ( value = "/import/{token}/http/test", method = RequestMethod.POST )
    public ModelAndView testImport ( @Valid @FormData ( "command" ) final Configuration data, final BindingResult result, final HttpServletRequest request )
    {
        final Map<String, Object> model = new HashMap<> ();

        model.put ( "command", data );

        final JobHandle job = this.jobManager.startJob ( DownloadTester.ID, data );

        model.put ( "job", job );

        return new ModelAndView ( "test", model );
    }

    @RequestMapping ( value = "/import/{token}/http/testComplete", method = RequestMethod.POST )
    public ModelAndView completeTest ( @RequestParameter ( "jobId" ) final String jobId, @PathVariable ( "token" ) final String token )
    {
        final Map<String, Object> model = new HashMap<> ();

        final JobHandle job = this.jobManager.getJob ( jobId );
        model.put ( "job", job );

        final String data = job.getRequest ().getData ();
        final Configuration cfg = this.gson.fromJson ( data, Configuration.class );

        model.put ( "configuration", cfg );
        model.put ( "request", ImportRequest.toJson ( HttpImporter.ID, data ) );
        model.put ( "cfgJson", job.getRequest ().getData () );
        model.put ( "result", TestResult.fromJson ( job.getResult () ) );
        model.put ( "token", token );

        return new ModelAndView ( "testResult", model );
    }

    @ControllerValidator ( formDataClass = Configuration.class )
    public void validateImport ( final Configuration cfg, final ValidationContext ctx )
    {
        final String url = cfg.getUrl ();
        if ( url == null || url.isEmpty () )
        {
            return;
        }

        if ( !url.startsWith ( "http://" ) && !url.startsWith ( "https://" ) )
        {
            ctx.error ( "url", "Only 'http' or 'https' URLs are supported" );
        }
    }
}
