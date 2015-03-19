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

import java.util.Comparator;

import de.dentrassi.osgi.web.LinkTarget;
import de.dentrassi.osgi.web.RequestMethod;

public interface Task
{
    public static enum State
    {
        TODO,
        DONE;
    }

    public State getState ();

    public String getTitle ();

    public String getDescription ();

    /**
     * Get an optional link target
     *
     * @return the link target or <code>null</code>
     */
    public LinkTarget getTarget ();

    public default RequestMethod getTargetRequestMethod ()
    {
        return RequestMethod.GET;
    }

    public int getPriority ();

    public default boolean isDone ()
    {
        return getState () == State.DONE;
    }

    public default boolean isOpen ()
    {
        return getState () != State.DONE;
    }

    public static final Comparator<Task> PRIORITY_COMPARATOR = PriorityComparator.INSTANCE;

}
