/*******************************************************************************
 * Copyright (c) 2014, 2015 Jens Reimann.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jens Reimann - initial API and implementation
 *******************************************************************************/
package de.dentrassi.pm.setup.web;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import de.dentrassi.osgi.web.Controller;
import de.dentrassi.osgi.web.LinkTarget;
import de.dentrassi.osgi.web.ModelAndView;
import de.dentrassi.osgi.web.RequestMapping;
import de.dentrassi.osgi.web.RequestMethod;
import de.dentrassi.osgi.web.ViewResolver;
import de.dentrassi.osgi.web.controller.ControllerInterceptor;
import de.dentrassi.osgi.web.controller.binding.BindingResult;
import de.dentrassi.osgi.web.controller.form.FormData;
import de.dentrassi.pm.common.web.InterfaceExtender;
import de.dentrassi.pm.common.web.Modifier;
import de.dentrassi.pm.common.web.menu.MenuEntry;
import de.dentrassi.pm.database.DatabaseConnectionData;
import de.dentrassi.pm.database.DatabaseSetup;
import de.dentrassi.pm.database.JdbcHelper;
import de.dentrassi.pm.sec.web.controller.Secured;
import de.dentrassi.pm.sec.web.controller.SecuredControllerInterceptor;
import de.dentrassi.pm.setup.web.internal.Activator;

@Controller
@RequestMapping ( value = "/config" )
@ViewResolver ( "/WEB-INF/views/%s.jsp" )
@Secured
@ControllerInterceptor ( SecuredControllerInterceptor.class )
public class ConfigController implements InterfaceExtender
{
    @Override
    public List<MenuEntry> getMainMenuEntries ( final HttpServletRequest request )
    {
        final List<MenuEntry> result = new LinkedList<> ();

        if ( request.getUserPrincipal () != null )
        {
            result.add ( new MenuEntry ( "Administration", 10_000, "Database Setup", 100, LinkTarget.createFromController ( ConfigController.class, "main" ), Modifier.DEFAULT, null, false ) );
        }

        return result;
    }

    @RequestMapping ( method = RequestMethod.GET )
    public ModelAndView main ()
    {
        final Map<String, Object> model = new HashMap<> ();

        try ( Configurator cfg = Configurator.create () )
        {
            final DatabaseConnectionData command = cfg.getDatabaseSettings ();

            fillData ( command, model );

            model.put ( "command", command );
        }

        return new ModelAndView ( "/config/index", model );
    }

    private boolean fillData ( final DatabaseConnectionData command, final Map<String, Object> model )
    {
        model.put ( "configured", Boolean.FALSE );

        boolean needUpdate = false;
        try
        {
            try ( DatabaseSetup setup = new DatabaseSetup ( command ) )
            {
                model.put ( "databaseSchemaVersion", setup.getSchemaVersion () );
                model.put ( "currentVersion", setup.getCurrentVersion () );
                model.put ( "configured", setup.isConfigured () );
                needUpdate = setup.isNeedUpgrade ();
            }
        }
        catch ( final Exception e )
        {
        }

        model.put ( "storageServicePresent", Activator.getTracker ().getService () != null );

        model.put ( "jdbcDrivers", JdbcHelper.getJdbcDrivers () );

        return needUpdate;
    }

    @RequestMapping ( method = RequestMethod.POST )
    public ModelAndView setup ( @Valid @FormData ( "command" ) final DatabaseConnectionData data, final BindingResult result )
    {
        final Map<String, Object> model = new HashMap<> ();
        model.put ( "command", data );

        model.put ( "jdbcDrivers", JdbcHelper.getJdbcDrivers () );

        if ( !result.hasErrors () )
        {
            // store

            try ( Configurator cfg = Configurator.create () )
            {
                cfg.setDatabaseSettings ( data );
            }

            // now wait until the configuration was performed in the background

            try
            {
                Activator.getTracker ().waitForService ( 5000 );
            }
            catch ( final InterruptedException e )
            {
            }
        }

        final boolean needUpgrade = fillData ( data, model );

        if ( needUpgrade )
        {
            return new ModelAndView ( "/config/index", model );
        }
        else
        {
            return new ModelAndView ( "redirect:/setup" );
        }
    }

    @RequestMapping ( value = "/databaseUpgrade", method = RequestMethod.POST )
    public ModelAndView upgrade ()
    {
        final Map<String, Object> model = new HashMap<> ();

        try
        {
            try ( Configurator cfg = Configurator.create () )
            {
                final DatabaseConnectionData data = cfg.getDatabaseSettings ();
                try ( DatabaseSetup setup = new DatabaseSetup ( data ) )
                {
                    setup.performUpgrade ();
                }
                fillData ( data, model );
            }

            return new ModelAndView ( "/config/upgrade", model );
        }
        catch ( final Throwable e )
        {
            model.clear ();
            model.put ( "error", e );
            return new ModelAndView ( "/config/upgradeFailed", model );
        }
    }
}
