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
package de.dentrassi.pm.system.web;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.annotation.HttpConstraint;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.dentrassi.osgi.web.Controller;
import de.dentrassi.osgi.web.LinkTarget;
import de.dentrassi.osgi.web.ModelAndView;
import de.dentrassi.osgi.web.RequestMapping;
import de.dentrassi.osgi.web.ViewResolver;
import de.dentrassi.osgi.web.controller.ControllerInterceptor;
import de.dentrassi.pm.common.web.InterfaceExtender;
import de.dentrassi.pm.common.web.menu.MenuEntry;
import de.dentrassi.pm.sec.web.controller.HttpContraintControllerInterceptor;
import de.dentrassi.pm.sec.web.controller.Secured;
import de.dentrassi.pm.sec.web.controller.SecuredControllerInterceptor;
import de.dentrassi.pm.system.ConfigurationBackupService;

@Controller
@ViewResolver ( "/WEB-INF/views/backup/%s.jsp" )
@RequestMapping ( "/system/backup" )
@Secured
@ControllerInterceptor ( SecuredControllerInterceptor.class )
@HttpConstraint
@ControllerInterceptor ( HttpContraintControllerInterceptor.class )
public class BackupController implements InterfaceExtender
{
    private ConfigurationBackupService service;

    public void setService ( final ConfigurationBackupService service )
    {
        this.service = service;
    }

    @RequestMapping
    public ModelAndView main ()
    {
        final Map<String, Object> model = new HashMap<> ();
        return new ModelAndView ( "index", model );
    }

    @RequestMapping ( "/export" )
    public void exportData ( final HttpServletResponse response ) throws IOException
    {
        response.setContentType ( "application/zip" );
        response.setHeader ( "Content-Disposition", String.format ( "attachment; filename=package-drone-backup-%1$tY%1$tm%1$td-%1$tH-%1$tM.zip", new Date () ) );
        response.setStatus ( HttpServletResponse.SC_OK );
        this.service.createConfigurationBackup ( response.getOutputStream () );
    }

    @Override
    public List<MenuEntry> getMainMenuEntries ( final HttpServletRequest request )
    {
        if ( request.isUserInRole ( "ADMIN" ) )
        {
            final List<MenuEntry> result = new LinkedList<> ();

            result.add ( new MenuEntry ( "System", 20_000, "Backup", 200, LinkTarget.createFromController ( BackupController.class, "main" ), null, null ) );

            return result;
        }

        return null;
    }
}
