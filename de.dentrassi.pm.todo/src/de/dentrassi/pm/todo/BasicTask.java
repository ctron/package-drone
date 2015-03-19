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
package de.dentrassi.pm.todo;

import de.dentrassi.osgi.web.LinkTarget;
import de.dentrassi.osgi.web.RequestMethod;

public class BasicTask implements Task
{
    private State state;

    private final String title;

    private final String description;

    private final LinkTarget target;

    private final int priority;

    private final RequestMethod targetRequestMethod;

    public BasicTask ( final String title, final int priority, final String description, final LinkTarget target )
    {
        this ( title, priority, description, target, null );
    }

    public BasicTask ( final String title, final int priority, final String description, final LinkTarget target, final RequestMethod method )
    {
        this.state = State.TODO;
        this.priority = priority;
        this.title = title;
        this.description = description;
        this.target = target;
        this.targetRequestMethod = method == null ? RequestMethod.GET : RequestMethod.POST;
    }

    @Override
    public RequestMethod getTargetRequestMethod ()
    {
        return this.targetRequestMethod;
    }

    public void setState ( final State state )
    {
        this.state = state;
    }

    @Override
    public State getState ()
    {
        return this.state;
    }

    @Override
    public String getTitle ()
    {
        return this.title;
    }

    @Override
    public String getDescription ()
    {
        return this.description;
    }

    @Override
    public LinkTarget getTarget ()
    {
        return this.target;
    }

    @Override
    public int getPriority ()
    {
        return this.priority;
    }
}
