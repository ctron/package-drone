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
package de.dentrassi.pm.storage.web.menu;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.ServiceTracker;

public class MenuManager
{
    public static class MenuEntry implements Comparable<MenuEntry>
    {
        private final String location;

        private final String label;

        private final int order;

        public MenuEntry ( final String location, final String label, final int order )
        {
            this.location = location;
            this.label = label;
            this.order = order;
        }

        public String getLocation ()
        {
            return this.location;
        }

        public String getLabel ()
        {
            return this.label;
        }

        public int getOrder ()
        {
            return this.order;
        }

        @Override
        public int compareTo ( final MenuEntry o )
        {
            return Integer.compare ( this.order, o.order );
        }
    }

    private final ServiceTracker<MenuExtender, MenuExtender> tracker;

    public MenuManager ()
    {
        this.tracker = new ServiceTracker<MenuExtender, MenuExtender> ( FrameworkUtil.getBundle ( MenuManager.class ).getBundleContext (), MenuExtender.class, null );
        this.tracker.open ();
    }

    public void close ()
    {
        this.tracker.close ();
    }

    public List<MenuEntry> getEntries ()
    {
        final List<MenuEntry> result = new LinkedList<> ();

        // this should be cached
        for ( final MenuExtender me : this.tracker.getServices ( new MenuExtender[0] ) )
        {
            result.addAll ( me.getEntries () );
        }

        Collections.sort ( result );

        return result;
    }

}
