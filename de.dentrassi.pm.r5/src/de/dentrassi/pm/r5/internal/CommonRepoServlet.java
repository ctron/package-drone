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
package de.dentrassi.pm.r5.internal;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.common.servlet.Handler;
import de.dentrassi.pm.r5.internal.handler.DownloadHandler;
import de.dentrassi.pm.r5.internal.handler.NotFoundHandler;
import de.dentrassi.pm.storage.Artifact;
import de.dentrassi.pm.storage.Channel;
import de.dentrassi.pm.storage.service.servlet.AbstractStorageServiceServlet;
import de.dentrassi.pm.storage.web.utils.ChannelCacheHandler;

public abstract class CommonRepoServlet extends AbstractStorageServiceServlet
{
    private static final long serialVersionUID = 1L;

    /**
     * This key points to the main index file which should be served
     */
    private final MetaKey keyToIndex;

    public CommonRepoServlet ( final MetaKey keyToIndex )
    {
        this.keyToIndex = keyToIndex;
    }

    protected Artifact findArtifact ( final HttpServletRequest request, final String artifactId )
    {
        return getService ( request ).getArtifact ( artifactId );
    }

    protected NotFoundHandler resourceNotFound ( final HttpServletRequest req )
    {
        return new NotFoundHandler ( String.format ( "Resource '%s' not found.%n", req.getPathInfo () ) );
    }

    protected Channel findChannel ( final HttpServletRequest request, final String channelIdOrName )
    {
        return getService ( request ).getChannelWithAlias ( channelIdOrName );
    }

    protected Handler findHandler ( final HttpServletRequest req, final HttpServletResponse resp )
    {
        final String path = req.getPathInfo ();

        if ( path == null || path.isEmpty () || "/".equals ( path ) )
        {
            return getHelpHandler ();
        }

        final String[] toks = req.getPathInfo ().split ( "\\/" );

        if ( toks.length == 2 )
        {
            final Channel channel = findChannel ( req, toks[1] );
            if ( channel == null )
            {
                return new NotFoundHandler ( String.format ( "Channel '%s' not found.", toks[1] ) );
            }

            final ChannelCacheHandler indexHandler = new ChannelCacheHandler ( this.keyToIndex );
            return new Handler () {
                @Override
                public void process ( final HttpServletRequest req, final HttpServletResponse resp ) throws Exception
                {
                    indexHandler.process ( channel, req, resp );
                }
            };
        }

        // - URL type #1 : /<base>/<channel>/artifact/<artifactId>
        // - URL type #2 : /<base>/<channel>/artifact/<artifactId>/<artifactName>
        if ( ( toks.length == 4 || toks.length == 5 ) && "artifact".equals ( toks[2] ) )
        {
            // we can ignore the channel id since artifact ids are globally unique
            final Artifact artifact = findArtifact ( req, toks[3] );
            if ( artifact == null )
            {
                return new NotFoundHandler ( String.format ( "Artifact '%s' not found.", toks[2] ) );
            }
            return new DownloadHandler ( artifact );
        }

        return null;
    }

    protected abstract Handler getHelpHandler ();

    @Override
    protected void doGet ( final HttpServletRequest req, final HttpServletResponse resp ) throws ServletException, IOException
    {
        Handler handler = findHandler ( req, resp );

        if ( handler == null )
        {
            handler = resourceNotFound ( req );
        }

        try
        {
            handler.prepare ();
            handler.process ( req, resp );
        }
        catch ( final IOException e )
        {
            throw e;
        }
        catch ( final Exception e )
        {
            throw new ServletException ( e );
        }
    }

}
