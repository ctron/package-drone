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
package de.dentrassi.pm.storage.service.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.ServiceTracker;

import de.dentrassi.pm.storage.service.StorageService;

/**
 * This is an abstract implementation for implementing servlets which require
 * the {@link StorageService}.
 */
public abstract class AbstractStorageServiceServlet extends HttpServlet
{

    private static final long serialVersionUID = 1L;

    private ServiceTracker<StorageService, StorageService> tracker;

    public AbstractStorageServiceServlet ()
    {
        super ();
    }

    @Override
    public void init () throws ServletException
    {
        super.init ();

        final BundleContext context = FrameworkUtil.getBundle ( getClass () ).getBundleContext ();
        this.tracker = new ServiceTracker<> ( context, StorageService.class, null );
        this.tracker.open ();
    }

    protected StorageService getService ()
    {
        return this.tracker.getService ();
    }

    @Override
    protected void service ( final HttpServletRequest request, final HttpServletResponse response ) throws ServletException, IOException
    {
        if ( getService () == null )
        {
            handleNoService ( request, response );
        }
        else
        {
            super.service ( request, response );
        }
    }

    protected void handleNoService ( final HttpServletRequest request, final HttpServletResponse response ) throws IOException
    {
        response.setStatus ( HttpServletResponse.SC_SERVICE_UNAVAILABLE );
        response.setContentType ( "text/plain" );
        response.getWriter ().write ( "Storage service unavailable" );
    }

    @Override
    public void destroy ()
    {
        this.tracker.close ();
        super.destroy ();
    }

}
