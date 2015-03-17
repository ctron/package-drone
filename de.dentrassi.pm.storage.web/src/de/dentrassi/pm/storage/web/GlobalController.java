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
package de.dentrassi.pm.storage.web;

import java.util.LinkedList;
import java.util.List;

import javax.servlet.annotation.HttpConstraint;
import javax.servlet.http.HttpServletRequest;

import de.dentrassi.osgi.web.Controller;
import de.dentrassi.osgi.web.LinkTarget;
import de.dentrassi.osgi.web.ModelAndView;
import de.dentrassi.osgi.web.RequestMapping;
import de.dentrassi.osgi.web.RequestMethod;
import de.dentrassi.osgi.web.ViewResolver;
import de.dentrassi.osgi.web.controller.ControllerInterceptor;
import de.dentrassi.pm.common.web.InterfaceExtender;
import de.dentrassi.pm.common.web.menu.MenuEntry;
import de.dentrassi.pm.sec.web.controller.HttpContraintControllerInterceptor;
import de.dentrassi.pm.sec.web.controller.Secured;
import de.dentrassi.pm.sec.web.controller.SecuredControllerInterceptor;
import de.dentrassi.pm.storage.service.StorageService;

@Secured
@Controller
@ViewResolver ( "/WEB-INF/views/global/%s.jsp" )
@ControllerInterceptor ( SecuredControllerInterceptor.class )
@HttpConstraint ( rolesAllowed = "MANAGER" )
@ControllerInterceptor ( HttpContraintControllerInterceptor.class )
public class GlobalController implements InterfaceExtender
{

    private StorageService service;

    public void setService ( final StorageService service )
    {
        this.service = service;
    }

    @RequestMapping ( value = "/system/storage" )
    public ModelAndView index ()
    {
        return new ModelAndView ( "index" );
    }

    @RequestMapping ( value = "/system/storage/wipe", method = RequestMethod.POST )
    public ModelAndView wipe ()
    {
        this.service.wipeClean ();
        return new ModelAndView ( "redirect:/channel" );
    }

    @Override
    public List<MenuEntry> getMainMenuEntries ( final HttpServletRequest request )
    {
        final List<MenuEntry> result = new LinkedList<> ();

        if ( request.isUserInRole ( "MANAGER" ) )
        {
            result.add ( new MenuEntry ( "System", Integer.MAX_VALUE, "Storage", 200, LinkTarget.createFromController ( GlobalController.class, "index" ), null, null ) );
        }

        return result;
    }
}
