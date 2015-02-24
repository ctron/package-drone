/*******************************************************************************
 * Copyright (c) 2014 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package de.dentrassi.pm.r5;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dentrassi.pm.common.servlet.Handler;
import de.dentrassi.pm.r5.handler.DownloadHandler;
import de.dentrassi.pm.r5.handler.HelpHandler;
import de.dentrassi.pm.r5.handler.IndexHandler;
import de.dentrassi.pm.r5.handler.NotFoundHandler;
import de.dentrassi.pm.storage.Artifact;
import de.dentrassi.pm.storage.Channel;
import de.dentrassi.pm.storage.service.StorageService;

public class R5Servlet extends HttpServlet
{
    private final static Logger logger = LoggerFactory.getLogger ( R5Servlet.class );

    private static final long serialVersionUID = 1L;

    private ServiceTracker<StorageService, StorageService> tracker;

    @Override
    public void init () throws ServletException
    {
        super.init ();

        final BundleContext context = FrameworkUtil.getBundle ( R5Servlet.class ).getBundleContext ();
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
        Handler handler = findHandler ( req );
        if ( handler == null )
        {
            handler = resourceNotFound ( req );
        }

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

    private Handler findHandler ( final HttpServletRequest req )
    {
        final String[] toks = req.getPathInfo ().split ( "\\/" );

        if ( "/".equals ( req.getPathInfo () ) )
        {
            return new HelpHandler ();
        }

        if ( toks.length == 2 )
        {
            final Channel channel = findChannel ( toks[1] );
            if ( channel == null )
            {
                return new NotFoundHandler ( String.format ( "Channel '%s' not found.", toks[1] ) );
            }
            return new IndexHandler ( channel );
        }

        // - URL type #1 : /r5/<channel>/artifact/<artifactId>
        // - URL type #2 : /r5/<channel>/artifact/<artifactId>/<artifactName>
        if ( ( toks.length == 4 || toks.length == 5 ) && "artifact".equals ( toks[2] ) )
        {
            // we can ignore the channel id since artifact ids are globally unique
            final Artifact artifact = findArtifact ( toks[3] );
            if ( artifact == null )
            {
                return new NotFoundHandler ( String.format ( "Artifact '%s' not found.", toks[2] ) );
            }
            return new DownloadHandler ( artifact );
        }

        return null;
    }

    private Artifact findArtifact ( final String artifactId )
    {
        return this.tracker.getService ().getArtifact ( artifactId );
    }

    protected NotFoundHandler resourceNotFound ( final HttpServletRequest req )
    {
        return new NotFoundHandler ( String.format ( "Resource '%s' not found.%n", req.getPathInfo () ) );
    }

    private Channel findChannel ( final String channelIdOrName )
    {
        return this.tracker.getService ().getChannelWithAlias ( channelIdOrName );
    }
}
