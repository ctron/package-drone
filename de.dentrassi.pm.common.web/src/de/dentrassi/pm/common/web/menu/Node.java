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
package de.dentrassi.pm.common.web.menu;

/**
 * A node in a ready to render menu
 */
public abstract class Node
{
    private final String label;

    private final String icon;

    public Node ( final String label, final String icon )
    {
        this.label = label;
        this.icon = icon;
    }

    public String getLabel ()
    {
        return this.label;
    }

    public String getIcon ()
    {
        return this.icon;
    }

}
