/*******************************************************************************
 * Copyright (c) 2014, 2015 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package de.dentrassi.pm.p2.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scada.utils.str.StringHelper;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dentrassi.pm.common.servlet.Handler;
import de.dentrassi.pm.storage.Channel;
import de.dentrassi.pm.storage.service.StorageService;

public class P2Servlet extends HttpServlet
{
    private static final Logger logger = LoggerFactory.getLogger ( P2Servlet.class );

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
    public void destroy ()
    {
        this.tracker.close ();
        super.destroy ();
    }

    @Override
    protected void service ( final HttpServletRequest req, final HttpServletResponse resp ) throws ServletException, IOException
    {
        logger.debug ( "Request: {} / {}", req.getMethod (), req.getPathInfo () );
        super.service ( req, resp );
    }

    @Override
    protected void doGet ( final HttpServletRequest req, final HttpServletResponse resp ) throws ServletException, IOException
    {
        final String path = req.getPathInfo ();
        final String paths[] = path.split ( "/" );

        if ( paths.length < 2 )
        {
            showHelp ( resp );
            return;
        }

        final String channelIdOrName = paths[1];
        final StorageService service = this.tracker.getService ();
        final Channel channel = service.getChannelWithAlias ( channelIdOrName );

        if ( channel == null )
        {
            notFound ( req, resp, String.format ( "Channel '%s' not found.", channelIdOrName ) );
            return;
        }

        if ( paths.length < 3 )
        {
            if ( !path.endsWith ( "/" ) )
            {
                resp.setStatus ( HttpServletResponse.SC_MOVED_PERMANENTLY );
                resp.sendRedirect ( req.getContextPath () + StringHelper.join ( paths, "/" ) + "/" );
                return;
            }
            process ( req, resp, new IndexHandler ( channel ) );
            return;
        }
        else if ( "p2.index".equals ( paths[2] ) && paths.length == 3 )
        {
            process ( req, resp, new P2IndexHandler ( channel ) );
        }
        else if ( "content.xml".equals ( paths[2] ) && paths.length == 3 )
        {
            process ( req, resp, new MetadataHandler ( channel, false ) );
        }
        else if ( "artifacts.xml".equals ( paths[2] ) && paths.length == 3 )
        {
            process ( req, resp, new ArtifactsHandler ( channel, false ) );
        }
        else if ( "content.jar".equals ( paths[2] ) && paths.length == 3 )
        {
            process ( req, resp, new MetadataHandler ( channel, true ) );
        }
        else if ( "artifacts.jar".equals ( paths[2] ) && paths.length == 3 )
        {
            process ( req, resp, new ArtifactsHandler ( channel, true ) );
        }
        else if ( "repo.zip".equals ( paths[2] ) && paths.length == 3 )
        {
            process ( req, resp, new ZippedHandler ( channel ) );
        }
        else if ( "plugins".equals ( paths[2] ) )
        {
            logger.warn ( "Download plugin: {}", req.getPathInfo () );
            process ( req, resp, new DownloadHandler ( channel, paths[3], "bundle" ) );
        }
        else if ( "features".equals ( paths[2] ) )
        {
            logger.warn ( "Download feature: {}", path );
            process ( req, resp, new DownloadHandler ( channel, paths[3], "eclipse.feature" ) );
        }
        else
        {
            logger.warn ( "Not found for: {}", path );
            notFound ( req, resp, "Resource not found: " + path );
        }
    }

    protected void notFound ( final HttpServletRequest req, final HttpServletResponse resp, final String message ) throws IOException
    {
        resp.setStatus ( HttpServletResponse.SC_NOT_FOUND );
        final PrintWriter w = resp.getWriter ();
        resp.setContentType ( "text/plain" );

        w.println ( message );
    }

    private void showHelp ( final HttpServletResponse resp ) throws IOException
    {
        resp.setStatus ( HttpServletResponse.SC_OK );
        resp.getWriter ().println ( "This is the package drone P2 adapter.\n\nAlways know where your towel is!" );
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

}
