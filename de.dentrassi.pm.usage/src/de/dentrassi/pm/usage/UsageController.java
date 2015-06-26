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
package de.dentrassi.pm.usage;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.annotation.HttpConstraint;
import javax.servlet.http.HttpServletRequest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.dentrassi.osgi.web.Controller;
import de.dentrassi.osgi.web.LinkTarget;
import de.dentrassi.osgi.web.ModelAndView;
import de.dentrassi.osgi.web.RequestMapping;
import de.dentrassi.osgi.web.ViewResolver;
import de.dentrassi.osgi.web.controller.ControllerInterceptor;
import de.dentrassi.pm.common.web.InterfaceExtender;
import de.dentrassi.pm.common.web.Modifier;
import de.dentrassi.pm.common.web.menu.MenuEntry;
import de.dentrassi.pm.sec.web.controller.HttpContraintControllerInterceptor;
import de.dentrassi.pm.sec.web.controller.Secured;
import de.dentrassi.pm.sec.web.controller.SecuredControllerInterceptor;

@Controller
@Secured
@HttpConstraint ( rolesAllowed = "ADMIN" )
@ControllerInterceptor ( SecuredControllerInterceptor.class )
@ControllerInterceptor ( HttpContraintControllerInterceptor.class )
@ViewResolver ( "/WEB-INF/views/%s.jsp" )
@RequestMapping ( "/system/usage" )
public class UsageController implements InterfaceExtender
{
    private static final List<MenuEntry> MAIN_ENTRIES = Collections.singletonList ( new MenuEntry ( "System", Integer.MAX_VALUE, "Usage", 6_000, LinkTarget.createFromController ( UsageController.class, "main" ), Modifier.DEFAULT, null ) );

    private PingJob pingJob;

    public void setPingJob ( final PingJob pingJob )
    {
        this.pingJob = pingJob;
    }

    @RequestMapping ( "/" )
    public ModelAndView main ()
    {
        final Map<String, Object> data = new HashMap<> ( 3 );

        data.put ( "enabled", this.pingJob.isActive () );
        data.put ( "data", makeJson ( this.pingJob.buildStatistics () ) );
        data.put ( "lastPingTimestamp", this.pingJob.getLastPing () );

        return new ModelAndView ( "index", data );
    }

    private String makeJson ( final Statistics stats )
    {
        final GsonBuilder gb = new GsonBuilder ();
        gb.setPrettyPrinting ();
        final Gson g = gb.create ();
        return g.toJson ( stats );
    }

    @Override
    public List<MenuEntry> getMainMenuEntries ( final HttpServletRequest request )
    {
        if ( request.isUserInRole ( "ADMIN" ) )
        {
            return MAIN_ENTRIES;
        }
        else
        {
            return null;
        }
    }
}
