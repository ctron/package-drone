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
package de.dentrassi.osgi.job.web.internal;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.HttpConstraint;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dentrassi.osgi.job.JobFactoryDescriptor;
import de.dentrassi.osgi.job.JobHandle;
import de.dentrassi.osgi.job.JobManager;
import de.dentrassi.osgi.web.Controller;
import de.dentrassi.osgi.web.LinkTarget;
import de.dentrassi.osgi.web.ModelAndView;
import de.dentrassi.osgi.web.RequestMapping;
import de.dentrassi.osgi.web.ViewResolver;
import de.dentrassi.osgi.web.controller.ControllerInterceptor;
import de.dentrassi.osgi.web.controller.binding.PathVariable;
import de.dentrassi.osgi.web.util.Requests;
import de.dentrassi.pm.sec.web.controller.HttpContraintControllerInterceptor;
import de.dentrassi.pm.sec.web.controller.Secured;
import de.dentrassi.pm.sec.web.controller.SecuredControllerInterceptor;

@Controller
@ViewResolver ( "/WEB-INF/views/%s.jsp" )
@Secured
@RequestMapping ( "/job" )
@ControllerInterceptor ( SecuredControllerInterceptor.class )
@HttpConstraint ( rolesAllowed = "MANAGER" )
@ControllerInterceptor ( HttpContraintControllerInterceptor.class )
public class JobController
{
    private final static Logger logger = LoggerFactory.getLogger ( JobController.class );

    private JobManager manager;

    public void setManager ( final JobManager manager )
    {
        this.manager = manager;
    }

    @RequestMapping ( "/{id}/monitor" )
    public ModelAndView view ( @PathVariable ( "id" ) final String id )
    {
        final JobHandle job = this.manager.getJob ( id );

        final Map<String, Object> model = new HashMap<> ();
        model.put ( "job", job );
        return new ModelAndView ( "monitor", model );
    }

    @RequestMapping ( "/{id}/result" )
    public ModelAndView result ( @PathVariable ( "id" ) final String id, final HttpServletRequest request, final HttpServletResponse response ) throws ServletException, IOException
    {
        final JobHandle job = this.manager.getJob ( id );

        if ( job != null && job.isComplete () && job.getError () != null )
        {
            // show default error page
            return defaultResult ( job );
        }

        final String factoryId = job.getRequest ().getFactoryId ();
        final JobFactoryDescriptor desc = this.manager.getFactory ( factoryId );

        if ( desc == null )
        {
            return defaultResult ( job );
        }

        final LinkTarget target = desc.getResultTarget ();
        if ( target == null )
        {
            return defaultResult ( job );
        }

        final LinkTarget url = target.expand ( Collections.singletonMap ( "id", id ) );

        logger.debug ( "Forwarding to job result view: {}", url );

        if ( url.getUrl ().equals ( Requests.getOriginalPath ( request ) ) )
        {
            throw new IllegalStateException ( String.format ( "Illegal redirect to same URL: %s", url.getUrl () ) );
        }

        final RequestDispatcher rd = request.getRequestDispatcher ( url.getUrl () );
        rd.forward ( request, response );

        return null;
    }

    protected ModelAndView defaultResult ( final JobHandle job )
    {
        final Map<String, Object> model = new HashMap<> ();
        model.put ( "job", job );
        return new ModelAndView ( "defaultResult", model );
    }
}
