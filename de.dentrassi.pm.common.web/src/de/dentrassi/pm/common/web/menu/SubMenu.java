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
package de.dentrassi.pm.common.web.menu;

import java.util.Collections;
import java.util.List;

import de.dentrassi.pm.common.web.Modifier;

public class SubMenu extends Node
{
    private final List<Node> nodes;

    private final Modifier modifier;

    public SubMenu ( final String label, final List<Node> nodes )
    {
        this ( label, nodes, Modifier.DEFAULT );
    }

    public SubMenu ( final String label, final List<Node> nodes, final Modifier modifier )
    {
        super ( label, null );
        this.nodes = Collections.unmodifiableList ( nodes );
        this.modifier = modifier;
    }

    public List<Node> getNodes ()
    {
        return this.nodes;
    }

    public Modifier getModifier ()
    {
        return this.modifier;
    }
}
