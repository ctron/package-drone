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
package de.dentrassi.pm.setup.web;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dentrassi.osgi.web.Controller;
import de.dentrassi.osgi.web.LinkTarget;
import de.dentrassi.osgi.web.ModelAndView;
import de.dentrassi.osgi.web.RequestMapping;
import de.dentrassi.osgi.web.ViewResolver;
import de.dentrassi.pm.database.DatabaseSetup;
import de.dentrassi.pm.todo.BasicTask;
import de.dentrassi.pm.todo.Task;
import de.dentrassi.pm.todo.Task.State;

@Controller
@ViewResolver ( "/WEB-INF/views/%s.jsp" )
@RequestMapping ( "/setup" )
public class SetupController
{
    private final static Logger logger = LoggerFactory.getLogger ( SetupController.class );

    private List<Task> getTasks ( final HttpServletRequest request )
    {
        boolean needUpgrade = false;
        boolean configured = false;

        try ( Configurator cfg = Configurator.create () )
        {
            try ( DatabaseSetup db = new DatabaseSetup ( cfg.getDatabaseSettings () ) )
            {
                needUpgrade = db.isNeedUpgrade ();
                configured = db.isConfigured ();
            }
        }
        catch ( final Exception e )
        {
            logger.warn ( "Failed to load tasks", e );
        }

        final List<Task> result = new LinkedList<> ();

        final boolean loggedIn = request.getUserPrincipal () != null;
        {
            final BasicTask task = new BasicTask ( "Sign is as admin user", 1, "Sign in with the default admin user. Unless you changed the setup the default name is <code>admin</code> and the password/token is printed out on the console of the server application. <br/><br/> Alternatively the token is written to the file <code>${user.home}/.drone-admin-token</code>.", new LinkTarget ( "/login" ) );
            if ( loggedIn )
            {
                task.setState ( State.DONE );
            }
            result.add ( task );
        }

        {
            final BasicTask task = new BasicTask ( "Configure the database connection", 2, "Head over to the <q>Database configuration</q> section and enter your database settings. Be sure you have a database instance set up.", loggedIn ? new LinkTarget ( "/config" ) : null );
            if ( configured )
            {
                task.setState ( State.DONE );
            }
            result.add ( task );
        }

        {
            final BasicTask task = new BasicTask ( "Install or upgdate the database schema", 3, "After the database connection is set up correctly, it may be necessary to install or upgrade the database schema. In this case a button will appear on the right side of the database connection form. <strong>Press it</strong>!", null );
            if ( !needUpgrade && configured )
            {
                task.setState ( State.DONE );
            }
            result.add ( task );
        }

        {
            final BasicTask task = new BasicTask ( "Configure the mail service", 4, "You will need to configure a mail server which Package Drone can use to sent e-mails.", loggedIn ? new LinkTarget ( "/default.mail/config" ) : null );

            final BundleContext ctx = FrameworkUtil.getBundle ( SetupController.class ).getBundleContext ();
            final boolean mailPresent = ctx.getServiceReference ( "de.dentrassi.pm.mail.service.MailService" ) != null;

            if ( mailPresent )
            {
                task.setState ( State.DONE );
            }

            result.add ( task );
        }

        return result;
    }

    @RequestMapping
    public ModelAndView index ( final HttpServletRequest request )
    {
        final Map<String, Object> model = new HashMap<> ();

        model.put ( "tasks", getTasks ( request ) );

        return new ModelAndView ( "setup/index", model );
    }
}
