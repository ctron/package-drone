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
package org.eclipse.packagedrone.repo.importer.aether.web;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.annotation.HttpConstraint;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.packagedrone.job.JobHandle;
import org.eclipse.packagedrone.job.JobManager;
import org.eclipse.packagedrone.job.JobRequest;
import org.eclipse.packagedrone.repo.importer.aether.AetherImporter;
import org.eclipse.packagedrone.repo.importer.aether.ImportConfiguration;
import org.eclipse.packagedrone.repo.importer.aether.MavenCoordinates;
import org.eclipse.packagedrone.repo.importer.aether.SimpleArtifactConfiguration;
import org.eclipse.packagedrone.repo.importer.web.ImportRequest;
import org.eclipse.packagedrone.sec.web.controller.HttpContraintControllerInterceptor;
import org.eclipse.packagedrone.sec.web.controller.Secured;
import org.eclipse.packagedrone.sec.web.controller.SecuredControllerInterceptor;
import org.eclipse.packagedrone.web.Controller;
import org.eclipse.packagedrone.web.ModelAndView;
import org.eclipse.packagedrone.web.RequestMapping;
import org.eclipse.packagedrone.web.RequestMethod;
import org.eclipse.packagedrone.web.ViewResolver;
import org.eclipse.packagedrone.web.controller.ControllerInterceptor;
import org.eclipse.packagedrone.web.controller.binding.BindingResult;
import org.eclipse.packagedrone.web.controller.binding.ExceptionError;
import org.eclipse.packagedrone.web.controller.binding.PathVariable;
import org.eclipse.packagedrone.web.controller.binding.RequestParameter;
import org.eclipse.packagedrone.web.controller.form.FormData;
import org.eclipse.packagedrone.web.controller.validator.ControllerValidator;
import org.eclipse.packagedrone.web.controller.validator.ValidationContext;

import com.google.gson.GsonBuilder;

@Secured
@Controller
@ViewResolver ( "/WEB-INF/views/%s.jsp" )
@ControllerInterceptor ( SecuredControllerInterceptor.class )
@HttpConstraint ( rolesAllowed = "MANAGER" )
@ControllerInterceptor ( HttpContraintControllerInterceptor.class )
public class ConfigurationController
{
    private final GsonBuilder gson = new GsonBuilder ();

    private JobManager jobManager;

    public void setJobManager ( final JobManager jobManager )
    {
        this.jobManager = jobManager;
    }

    @RequestMapping ( value = "/import/{token}/aether/start", method = RequestMethod.GET )
    public ModelAndView configure ( @RequestParameter ( value = "configuration",
            required = false ) final SimpleArtifactConfiguration cfg)
    {
        final Map<String, Object> model = new HashMap<> ();

        if ( cfg != null )
        {
            model.put ( "command", cfg );
        }
        else
        {
            model.put ( "command", new SimpleArtifactConfiguration () );
        }

        return new ModelAndView ( "configure", model );
    }

    @RequestMapping ( value = "/import/{token}/aether/start", method = RequestMethod.POST )
    public ModelAndView configurePost ( @Valid @FormData ( "command" ) final SimpleArtifactConfiguration data, final BindingResult result)
    {
        final Map<String, Object> model = new HashMap<> ();

        model.put ( "ok", !result.hasErrors () );

        return new ModelAndView ( "configure", model );
    }

    @RequestMapping ( value = "/import/{token}/aether/test", method = RequestMethod.POST )
    public ModelAndView testImport ( @Valid @FormData ( "command" ) final SimpleArtifactConfiguration data, final BindingResult result, final HttpServletRequest request)
    {
        if ( result.hasErrors () )
        {
            return configurePost ( data, result );
        }

        final Map<String, Object> model = new HashMap<> ();

        model.put ( "command", data );

        final ImportConfiguration imp = new ImportConfiguration ();
        imp.setRepositoryUrl ( data.getUrl () );

        final MavenCoordinates main = MavenCoordinates.fromString ( data.getCoordinates () );

        imp.getCoordinates ().add ( main );
        if ( data.isIncludeSources () )
        {
            final MavenCoordinates sources = new MavenCoordinates ( main );
            sources.setClassifier ( "sources" );
            imp.getCoordinates ().add ( sources );
        }

        final Map<String, String> properties = new HashMap<> ( 1 );
        properties.put ( "originalConfig", this.gson.create ().toJson ( data ) );
        final JobRequest jr = new JobRequest ( AetherTester.ID, this.gson.create ().toJson ( imp ), properties );
        final JobHandle job = this.jobManager.startJob ( jr );

        model.put ( "job", job );

        return new ModelAndView ( "test", model );
    }

    @RequestMapping ( value = "/import/{token}/aether/testComplete", method = RequestMethod.POST )
    public ModelAndView completeTest ( @RequestParameter ( "jobId" ) final String jobId, @PathVariable ( "token" ) final String token)
    {
        final Map<String, Object> model = new HashMap<> ();

        final JobHandle job = this.jobManager.getJob ( jobId );

        model.put ( "job", job );

        final String data = job.getRequest ().getData ();
        final ImportConfiguration cfg = this.gson.create ().fromJson ( data, ImportConfiguration.class );

        model.put ( "configuration", cfg );

        model.put ( "request", ImportRequest.toJson ( AetherImporter.ID, data ) );
        model.put ( "cfgJson", job.getProperties ().get ( "originalConfig" ) );
        model.put ( "token", token );

        if ( job != null && job.isFailed () )
        {
            model.put ( "error", job.getError () );
            return new ModelAndView ( "testFailed", model );
        }
        else
        {
            model.put ( "result", AetherResult.fromJson ( job.getResult () ) );
            return new ModelAndView ( "testResult", model );
        }
    }

    @ControllerValidator ( formDataClass = SimpleArtifactConfiguration.class )
    public void validateImportConfiguration ( final SimpleArtifactConfiguration cfg, final ValidationContext ctx )
    {
        try
        {
            final String coords = cfg.getCoordinates ();
            if ( coords != null && !coords.isEmpty () )
            {
                new DefaultArtifact ( coords );
            }
        }
        catch ( final Exception e )
        {
            ctx.error ( "coordinates", new ExceptionError ( e ) );
        }
    }
}
