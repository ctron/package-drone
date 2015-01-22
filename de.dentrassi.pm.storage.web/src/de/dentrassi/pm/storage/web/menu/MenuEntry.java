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

/**
 * A menu entry from an extension point
 */
public class MenuEntry implements Comparable<MenuEntry>
{
    private final String category;

    private final String label;

    private final LinkTarget target;

    private final Modifier modifier;

    private final int categoryOrder;

    private final int entryOrder;

    private final boolean newWindow;

    private final String icon;

    public MenuEntry ( final String category, final int categoryOrder, final String label, final int entryOrder, final LinkTarget target, final Modifier modifier, final String icon )
    {
        this ( category, categoryOrder, label, entryOrder, target, modifier, icon, false );
    }

    public MenuEntry ( final String category, final int categoryOrder, final String label, final int entryOrder, final LinkTarget target, final Modifier modifier, final String icon, final boolean newWindow )
    {
        this.category = category;
        this.categoryOrder = category != null ? categoryOrder : entryOrder;
        this.label = label;
        this.entryOrder = category != null ? entryOrder : 0;
        this.target = target;
        this.modifier = modifier;
        this.icon = icon;
        this.newWindow = newWindow;
    }

    public MenuEntry ( final String label, final int entryOrder, final LinkTarget target, final Modifier modifier, final String icon )
    {
        this.category = null;
        this.categoryOrder = entryOrder;
        this.label = label;
        this.entryOrder = 0;
        this.target = target;
        this.modifier = modifier;
        this.icon = icon;
        this.newWindow = false;
    }

    public String getIcon ()
    {
        return this.icon;
    }

    public boolean isNewWindow ()
    {
        return this.newWindow;
    }

    public String getCategory ()
    {
        return this.category;
    }

    public int getCategoryOrder ()
    {
        return this.categoryOrder;
    }

    public int getEntryOrder ()
    {
        return this.entryOrder;
    }

    public String getLabel ()
    {
        return this.label;
    }

    public LinkTarget getTarget ()
    {
        return this.target;
    }

    public Modifier getModifier ()
    {
        return this.modifier;
    }

    protected String getMainLabel ()
    {
        if ( this.category != null )
        {
            return this.category;
        }
        return this.label;
    }

    @Override
    public int compareTo ( final MenuEntry o )
    {
        // first by category
        int rc = Integer.compare ( this.categoryOrder, o.categoryOrder );
        if ( rc != 0 )
        {
            return rc;
        }

        // then by entry
        rc = Integer.compare ( this.entryOrder, o.entryOrder );
        if ( rc != 0 )
        {
            return rc;
        }

        // last by label
        return getMainLabel ().compareTo ( o.getMainLabel () );
    }
}
