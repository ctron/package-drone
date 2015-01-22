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
package de.dentrassi.pm.storage.web.menu;

import de.dentrassi.osgi.web.LinkTarget;
import de.dentrassi.pm.storage.web.Modifier;

public class Entry extends Node
{
    private final LinkTarget target;

    private final Modifier modifier;

    private final boolean newWindow;

    public Entry ( final String label, final LinkTarget target, final Modifier modifier, final String icon, final boolean newWindow )
    {
        super ( label, icon );
        this.target = target;
        this.modifier = modifier;

        this.newWindow = newWindow;
    }

    public Modifier getModifier ()
    {
        return this.modifier;
    }

    public LinkTarget getTarget ()
    {
        return this.target;
    }

    public boolean isNewWindow ()
    {
        return this.newWindow;
    }
}
