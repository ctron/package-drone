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

import static javax.servlet.annotation.ServletSecurity.EmptyRoleSemantic.PERMIT;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.annotation.HttpConstraint;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dentrassi.osgi.web.Controller;
import de.dentrassi.osgi.web.LinkTarget;
import de.dentrassi.osgi.web.ModelAndView;
import de.dentrassi.osgi.web.RequestMapping;
import de.dentrassi.osgi.web.RequestMethod;
import de.dentrassi.osgi.web.ViewResolver;
import de.dentrassi.osgi.web.controller.ControllerInterceptor;
import de.dentrassi.osgi.web.controller.binding.RequestParameter;
import de.dentrassi.osgi.web.util.BasicAuthentication;
import de.dentrassi.pm.common.web.CommonController;
import de.dentrassi.pm.common.web.InterfaceExtender;
import de.dentrassi.pm.common.web.menu.MenuEntry;
import de.dentrassi.pm.database.Configurator;
import de.dentrassi.pm.database.DatabaseSetup;
import de.dentrassi.pm.sec.UserInformation;
import de.dentrassi.pm.sec.service.LoginException;
import de.dentrassi.pm.sec.service.SecurityService;
import de.dentrassi.pm.sec.web.controller.HttpContraintControllerInterceptor;
import de.dentrassi.pm.sec.web.controller.Secured;
import de.dentrassi.pm.sec.web.controller.SecuredControllerInterceptor;
import de.dentrassi.pm.storage.service.StorageService;
import de.dentrassi.pm.system.ConfigurationBackupService;

@Controller
@ViewResolver ( "/WEB-INF/views/backup/%s.jsp" )
@RequestMapping ( "/system/backup" )
@Secured
@ControllerInterceptor ( SecuredControllerInterceptor.class )
@HttpConstraint ( rolesAllowed = "ADMIN" )
@ControllerInterceptor ( HttpContraintControllerInterceptor.class )
public class BackupController implements InterfaceExtender
{

    private final static Logger logger = LoggerFactory.getLogger ( BackupController.class );

    private ConfigurationBackupService service;

    private SecurityService securityService;

    public void setService ( final ConfigurationBackupService service )
    {
        this.service = service;
    }

    public void setSecurityService ( final SecurityService securityService )
    {
        this.securityService = securityService;
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

    @RequestMapping ( value = "/provision", method = RequestMethod.POST )
    @Secured ( false )
    @HttpConstraint ( PERMIT )
    public void provision ( final HttpServletRequest request, final HttpServletResponse response ) throws IOException
    {
        provision ( request, response, null );
    }

    @RequestMapping ( value = "/provisionWithSchema", method = RequestMethod.POST )
    @Secured ( false )
    @HttpConstraint ( PERMIT )
    public void provisionWithSchema ( final HttpServletRequest request, final HttpServletResponse response ) throws IOException
    {
        provision ( request, response, ( ) -> {
            try ( Configurator cfg = Configurator.create () )
            {
                try ( DatabaseSetup setup = new DatabaseSetup ( cfg.getDatabaseSettings () ) )
                {
                    setup.performUpgrade ();
                }
            }
        } );
    }

    protected void provision ( final HttpServletRequest request, final HttpServletResponse response, final Runnable afterwards ) throws IOException
    {
        final String[] authToks = BasicAuthentication.parseAuthorization ( request );
        if ( authToks == null )
        {
            BasicAuthentication.request ( response, "provision", "Please authenticate" );
            return;
        }

        UserInformation user;
        try
        {
            user = this.securityService.login ( authToks[0], authToks[1] );
            if ( user == null )
            {
                quickResponse ( response, HttpServletResponse.SC_FORBIDDEN, "Not allowed" );
                return;
            }
        }
        catch ( final LoginException e )
        {
            quickResponse ( response, HttpServletResponse.SC_FORBIDDEN, "Not allowed" );
            return;
        }

        try
        {
            this.service.provisionConfiguration ( request.getInputStream () );

            waitForService ();

            if ( afterwards != null )
            {
                afterwards.run ();
            }

            quickResponse ( response, HttpServletResponse.SC_OK, "OK" );
        }
        catch ( final Exception e )
        {
            logger.warn ( "Failed to import configuration", e );
            quickResponse ( response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to import configuration" );
            return;
        }
    }

    @RequestMapping ( value = "/import", method = RequestMethod.POST )
    public ModelAndView restoreData ( @RequestParameter ( "file" ) final Part part )
    {
        try
        {
            this.service.restoreConfiguration ( part.getInputStream () );
            waitForService ();
            return new ModelAndView ( "redirect:/system/backup" );
        }
        catch ( final Exception e )
        {
            // we require ADMIN permissions, so we can show the stack trace
            return CommonController.createError ( "Restore", "Failed to restore configuration", e, true );
        }
    }

    private void waitForService ()
    {
        final ServiceTracker<?, ?> tracker = new ServiceTracker<> ( FrameworkUtil.getBundle ( BackupController.class ).getBundleContext (), StorageService.class, null );
        tracker.open ();
        try
        {
            tracker.waitForService ( 5_000 ); // wait 5 seconds
        }
        catch ( final InterruptedException e )
        {
        }
        finally
        {
            tracker.close ();
        }
    }

    protected void quickResponse ( final HttpServletResponse response, final int statusCode, final String message ) throws IOException
    {
        response.setStatus ( statusCode );
        response.setContentType ( "text/plain" );
        response.getWriter ().write ( message );
    }

    @Override
    public List<MenuEntry> getMainMenuEntries ( final HttpServletRequest request )
    {
        if ( request.isUserInRole ( "ADMIN" ) )
        {
            final List<MenuEntry> result = new LinkedList<> ();

            result.add ( new MenuEntry ( "System", 20_000, "Configuration", 200, LinkTarget.createFromController ( BackupController.class, "main" ), null, null ) );

            return result;
        }

        return null;
    }
}
