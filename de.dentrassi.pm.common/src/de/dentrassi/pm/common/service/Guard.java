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
package de.dentrassi.pm.common.service;

import javax.persistence.EntityManager;

public interface Guard
{
    public default void before ( final EntityManager entityManager )
    {
    }

    public default void beforeCommit ( final Object result, final EntityManager entityManager )
    {
    }

    public default void afterCommit ()
    {
    }

    public default void beforeRollback ( final Throwable throwable, final EntityManager entityManager )
    {
    }

    public default void afterRollback ()
    {
    }

    public default void afterAll ()
    {
    }
}
