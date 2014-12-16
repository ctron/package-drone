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
package de.dentrassi.pm.storage.web.tags;

import de.dentrassi.pm.storage.Channel;

public class Functions
{
    public static String channel ( final Channel channel )
    {
        if ( channel.getName () == null )
        {
            return channel.getId ();
        }
        else
        {
            return String.format ( "%s (%s)", channel.getName (), channel.getId () );
        }
    }
}
