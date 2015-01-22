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
package de.dentrassi.pm.storage.web.setup;

import java.util.HashMap;
import java.util.Map;

import javax.validation.Valid;

import de.dentrassi.osgi.web.Controller;
import de.dentrassi.osgi.web.LinkTarget;
import de.dentrassi.osgi.web.ModelAndView;
import de.dentrassi.osgi.web.RequestMapping;
import de.dentrassi.osgi.web.RequestMethod;
import de.dentrassi.osgi.web.ViewResolver;
import de.dentrassi.osgi.web.controller.binding.BindingResult;
import de.dentrassi.osgi.web.controller.form.FormData;
import de.dentrassi.pm.database.DatabaseConnectionData;
import de.dentrassi.pm.database.DatabaseSetup;
import de.dentrassi.pm.database.JdbcHelper;
import de.dentrassi.pm.storage.web.Modifier;
import de.dentrassi.pm.storage.web.internal.Activator;
import de.dentrassi.pm.storage.web.menu.DefaultMenuExtender;
import de.dentrassi.pm.storage.web.menu.MenuEntry;

@Controller
@RequestMapping ( value = "/setup" )
@ViewResolver ( "/WEB-INF/views/%s.jsp" )
public class SetupController extends DefaultMenuExtender
{
    public SetupController ()
    {
        addEntry ( new MenuEntry ( "Administration", 10_000, "Database Setup", 100, new LinkTarget ( "/setup" ), Modifier.DEFAULT, null, false ) );
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

        return new ModelAndView ( "/setup/index", model );
    }

    private void fillData ( final DatabaseConnectionData command, final Map<String, Object> model )
    {
        model.put ( "configured", Boolean.FALSE );

        try
        {
            try ( DatabaseSetup setup = new DatabaseSetup ( command ) )
            {
                model.put ( "databaseSchemaVersion", setup.getSchemaVersion () );
                model.put ( "currentVersion", setup.getCurrentVersion () );
                model.put ( "configured", setup.isConfigured () );
            }
        }
        catch ( final Exception e )
        {
        }

        model.put ( "servicePresent", Activator.getTracker ().getStorageService () != null );
        model.put ( "jdbcDrivers", JdbcHelper.getJdbcDrivers () );
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

            Activator.getTracker ().waitForStorageService ( 5000 );
        }

        fillData ( data, model );

        return new ModelAndView ( "/setup/index", model );
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

            return new ModelAndView ( "/setup/upgrade", model );
        }
        catch ( final Throwable e )
        {
            model.clear ();
            model.put ( "error", e );
            return new ModelAndView ( "/setup/upgradeFailed", model );
        }
    }
}
