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
package de.dentrassi.pm.mail.web;

import org.osgi.framework.InvalidSyntaxException;

import de.dentrassi.osgi.web.LinkTarget;
import de.dentrassi.pm.mail.service.MailService;
import de.dentrassi.pm.todo.BasicTask;
import de.dentrassi.pm.todo.DefaultTaskProvider;
import de.dentrassi.pm.todo.Task;

public class MailServiceTaskProvider extends DefaultTaskProvider
{
    private final Task task = new BasicTask ( "Configure mail service", 1, "It is necessary to configure a mail service. Follow the link to configure the default mail service.", new LinkTarget ( "/default.mail/config" ) );

    private MailService service;

    public void setService ( final MailService service )
    {
        this.service = service;
        removeTask ( this.task );
    }

    public void unsetService ( final MailService service )
    {
        this.service = null;
        addTask ( this.task );
    }

    public void start () throws InvalidSyntaxException
    {
        if ( this.service == null )
        {
            addTask ( this.task );
        }
    }
}
