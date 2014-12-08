/*******************************************************************************
 * Copyright (c) 2014 Jens Reimann.
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
import de.dentrassi.osgi.web.ModelAndView;
import de.dentrassi.osgi.web.RequestMapping;
import de.dentrassi.osgi.web.RequestMethod;
import de.dentrassi.osgi.web.ViewResolver;
import de.dentrassi.osgi.web.controller.binding.BindingResult;
import de.dentrassi.osgi.web.controller.form.FormData;
import de.dentrassi.pm.storage.web.Activator;
import de.dentrassi.pm.storage.web.menu.DefaultMenuExtender;

@Controller
@RequestMapping ( value = "/setup" )
@ViewResolver ( "/WEB-INF/views/%s.jsp" )
public class SetupController extends DefaultMenuExtender
{
    public SetupController ()
    {
        addEntry ( "/setup", "Setup", 100 );
    }

    @RequestMapping ( method = RequestMethod.GET )
    public ModelAndView main ()
    {
        final Map<String, Object> model = new HashMap<> ();

        try ( Configurator cfg = Configurator.create () )
        {
            final SetupData command = cfg.getDatabaseSettings ();

            model.put ( "command", command );
        }

        model.put ( "jdbcDrivers", JdbcHelper.getJdbcDrivers () );

        return new ModelAndView ( "setup/index", model );
    }

    @RequestMapping ( method = RequestMethod.POST )
    public ModelAndView setup ( @Valid
    @FormData ( "command" )
    final SetupData data, final BindingResult result )
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

        return new ModelAndView ( "setup/index", model );
    }
}
