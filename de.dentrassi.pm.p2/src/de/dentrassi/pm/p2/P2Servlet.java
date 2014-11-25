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
package de.dentrassi.pm.p2;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.ServiceTracker;

import de.dentrassi.pm.storage.service.Channel;
import de.dentrassi.pm.storage.service.StorageService;

public class P2Servlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    private ServiceTracker<StorageService, StorageService> tracker;

    @Override
    public void init () throws ServletException
    {
        super.init ();

        final BundleContext context = FrameworkUtil.getBundle ( P2Servlet.class ).getBundleContext ();
        this.tracker = new ServiceTracker<> ( context, StorageService.class, null );
        this.tracker.open ();
    }

    @Override
    protected void doGet ( final HttpServletRequest req, final HttpServletResponse resp ) throws ServletException, IOException
    {
        final String paths[] = req.getPathInfo ().split ( "/" );

        final String channelId = paths[1];

        final StorageService service = this.tracker.getService ();
        final Channel channel = service.getChannel ( channelId );

        if ( "content.xml".equals ( paths[2] ) && paths.length == 3 )
        {
            process ( req, resp, new MetadataHandler ( channel ) );
        }
        else if ( "artifacts.xml".equals ( paths[2] ) && paths.length == 3 )
        {
            process ( req, resp, new ArtifactsHandler ( channel ) );
        }
        else
        {
            resp.setStatus ( HttpServletResponse.SC_NOT_FOUND );
        }
    }

    private void process ( final HttpServletRequest req, final HttpServletResponse resp, final Handler handler ) throws ServletException
    {
        try
        {
            handler.prepare ();
            handler.process ( req, resp );
        }
        catch ( final Exception e )
        {
            throw new ServletException ( e );
        }
    }

    @Override
    public void destroy ()
    {
        this.tracker.close ();
        super.destroy ();
    }
}
