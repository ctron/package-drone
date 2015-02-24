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
package de.dentrassi.pm.p2.servlet;

import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.common.servlet.Handler;
import de.dentrassi.pm.p2.aspect.P2RepositoryAspect;
import de.dentrassi.pm.storage.Channel;

public abstract class AbstractChannelHandler implements Handler
{
    protected final Channel channel;

    public AbstractChannelHandler ( final Channel channel )
    {
        this.channel = channel;
    }

    protected String makeTitle ()
    {
        if ( this.channel.getName () != null )
        {
            return this.channel.getName ();
        }
        else
        {
            return this.channel.getId ();
        }
    }

    protected String makeExternalTitle ()
    {
        final String p2title = getTitle ();

        if ( p2title != null && !p2title.isEmpty () )
        {
            return p2title;
        }

        return String.format ( "Package Drone - Channel: %s", makeTitle () );
    }

    protected String getTitle ()
    {
        return this.channel.getMetaData ().get ( new MetaKey ( P2RepositoryAspect.ID, "title" ) );
    }

}
