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
package de.dentrassi.pm.setup.web;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

import de.dentrassi.osgi.web.Controller;
import de.dentrassi.osgi.web.LinkTarget;
import de.dentrassi.osgi.web.ModelAndView;
import de.dentrassi.osgi.web.RequestMapping;
import de.dentrassi.osgi.web.ViewResolver;
import de.dentrassi.pm.apm.StorageManager;
import de.dentrassi.pm.todo.BasicTask;
import de.dentrassi.pm.todo.Task;
import de.dentrassi.pm.todo.Task.State;

@Controller
@ViewResolver ( "/WEB-INF/views/%s.jsp" )
@RequestMapping ( "/setup" )
public class SetupController
{
    private StorageManager manager;

    public void setService ( final StorageManager manager )
    {
        this.manager = manager;
    }

    public void unsetService ( final StorageManager manager )
    {
        this.manager = null;
    }

    private List<Task> getTasks ( final HttpServletRequest request )
    {
        final List<Task> result = new LinkedList<> ();

        int idx = 1;

        final boolean loggedIn = request.getUserPrincipal () != null;
        {
            final BasicTask task = new BasicTask ( "Sign in as admin user", idx++, "Sign in with the default admin user. Unless you changed the setup the default name is <code>admin</code> and the password/token is printed out on the console of the server application. <br/><br/> Alternatively the token is written to the file <code>${user.home}/.drone-admin-token</code>.", new LinkTarget ( "/login" ) );
            if ( loggedIn )
            {
                task.setState ( State.DONE );
            }
            result.add ( task );
        }

        {
            final BasicTask task = new BasicTask ( "Configure the storage location", idx++, "Head over to the <q>Storage configuration</q> section and selection the location where Package Drone should store data.", loggedIn ? new LinkTarget ( "/config" ) : null );
            if ( this.manager != null )
            {
                task.setState ( State.DONE );
            }
            result.add ( task );
        }

        {
            final BasicTask task = new BasicTask ( "Configure the mail service", idx++, "You will need to configure a mail server which Package Drone can use to sent e-mails.", loggedIn ? new LinkTarget ( "/default.mail/config" ) : null );

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
