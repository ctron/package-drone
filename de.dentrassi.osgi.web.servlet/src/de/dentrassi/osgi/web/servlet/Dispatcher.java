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
package de.dentrassi.osgi.web.servlet;

import org.osgi.framework.BundleContext;

import de.dentrassi.osgi.web.servlet.internal.ServletContextImpl;

public class Dispatcher
{
    public static DispatcherHttpContext createContext ( final BundleContext context )
    {
        return new ServletContextImpl ( context );
    }
}
