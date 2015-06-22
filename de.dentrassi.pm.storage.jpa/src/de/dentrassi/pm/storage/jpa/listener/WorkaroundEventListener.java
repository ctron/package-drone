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
package de.dentrassi.pm.storage.jpa.listener;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.sessions.SessionEvent;
import org.eclipse.persistence.sessions.SessionEventAdapter;

/**
 * A workaround for issue 289771 [1]
 * <p>
 * [1] https://bugs.eclipse.org/bugs/show_bug.cgi?id=289771
 * </p>
 */
public class WorkaroundEventListener extends SessionEventAdapter
{
    @Override
    public void postLogin ( final SessionEvent event )
    {
        for ( final ClassDescriptor desc : event.getSession ().getDescriptors ().values () )
        {
            desc.getQueryManager ().setExpressionQueryCacheMaxSize ( 0 );;
        }
    }
}
