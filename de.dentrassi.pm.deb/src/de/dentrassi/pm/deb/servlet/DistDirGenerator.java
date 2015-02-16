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
package de.dentrassi.pm.deb.servlet;

import de.dentrassi.pm.deb.ChannelConfiguration;

public class DistDirGenerator
{

    private final ChannelConfiguration cfg;

    public DistDirGenerator ( final ChannelConfiguration cfg )
    {
        this.cfg = cfg;
    }

    @Override
    public String toString ()
    {
        final StringBuilder sb = new StringBuilder ();

        if ( this.cfg.getSigningService () != null )
        {
            sb.append ( "<li><a href=\"InRelease\">InRelease</a></li>" );
            sb.append ( "<li><a href=\"Release\">Release</a></li>" );
        }
        sb.append ( "<li><a href=\"Release.gpg\">Release.gpg</a></li>" );

        for ( final String comp : this.cfg.getComponents () )
        {
            sb.append ( "<li><a href=\"" + comp + "\">" + comp + "</li>" );
        }

        return sb.toString ();
    }
}
