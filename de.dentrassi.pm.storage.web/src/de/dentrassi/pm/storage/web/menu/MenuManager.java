/*******************************************************************************
 * Copyright (c) 2014, 2015 Jens Reimann.
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
import java.util.function.Function;

import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.ServiceTracker;

import de.dentrassi.pm.storage.web.InterfaceExtender;

public class MenuManager
{
    private final ServiceTracker<InterfaceExtender, InterfaceExtender> tracker;

    public MenuManager ()
    {
        this.tracker = new ServiceTracker<InterfaceExtender, InterfaceExtender> ( FrameworkUtil.getBundle ( MenuManager.class ).getBundleContext (), InterfaceExtender.class, null );
        this.tracker.open ();
    }

    public void close ()
    {
        this.tracker.close ();
    }

    public List<MenuEntry> getMainMenuEntries ()
    {
        // this should be cached
        return getEntries ( InterfaceExtender::getMainMenuEntries );
    }

    public List<MenuEntry> getActions ( final Object context )
    {
        return getEntries ( ( i ) -> i.getActions ( context ) );
    }

    protected List<MenuEntry> getEntries ( final Function<InterfaceExtender, List<MenuEntry>> func )
    {
        final List<MenuEntry> result = new LinkedList<> ();

        for ( final InterfaceExtender me : this.tracker.getTracked ().values () )
        {
            final List<MenuEntry> actions = func.apply ( me );
            if ( actions != null )
            {
                result.addAll ( actions );
            }
        }

        Collections.sort ( result );

        return result;
    }
}
