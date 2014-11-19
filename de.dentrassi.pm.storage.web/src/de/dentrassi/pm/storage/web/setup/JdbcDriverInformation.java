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
package de.dentrassi.pm.storage.web.setup;

public class JdbcDriverInformation
{
    private final String className;

    private final String name;

    private final String version;

    public JdbcDriverInformation ( final String className, final String name, final String version )
    {
        super ();
        this.className = className;
        this.name = name;
        this.version = version;
    }

    public String getClassName ()
    {
        return this.className;
    }

    public String getName ()
    {
        return this.name;
    }

    public String getVersion ()
    {
        return this.version;
    }

    @Override
    public String toString ()
    {
        return String.format ( "%s (%s)", this.name, this.version );
    }

}
