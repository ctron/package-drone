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
package de.dentrassi.osgi.job.web.internal;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.annotation.HttpConstraint;
import javax.servlet.http.HttpServletRequest;

import de.dentrassi.osgi.job.JobManager;
import de.dentrassi.osgi.job.web.Jobs;
import de.dentrassi.osgi.web.Controller;
import de.dentrassi.osgi.web.ModelAndView;
import de.dentrassi.osgi.web.RequestMapping;
import de.dentrassi.osgi.web.ViewResolver;
import de.dentrassi.osgi.web.controller.ControllerInterceptor;
import de.dentrassi.osgi.web.controller.binding.PathVariable;
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
    private JobManager manager;

    public void setManager ( final JobManager manager )
    {
        this.manager = manager;
    }

    @RequestMapping ( "/{id}/view" )
    public ModelAndView view ( @PathVariable ( "id" ) final String id, final HttpServletRequest request )
    {
        final Map<String, Object> model = new HashMap<> ();
        model.put ( "job", Jobs.get ( request, id ) );
        return new ModelAndView ( "viewJob", model );
    }
}
