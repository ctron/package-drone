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
package de.dentrassi.pm.common.web.task;

import de.dentrassi.osgi.web.LinkTarget;

public class BasicTask implements Task
{
    private State state;

    private final String title;

    private final String description;

    private final LinkTarget target;

    public BasicTask ( final String title, final String description, final LinkTarget target )
    {
        this.state = State.TODO;
        this.title = title;
        this.description = description;
        this.target = target;
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
}
