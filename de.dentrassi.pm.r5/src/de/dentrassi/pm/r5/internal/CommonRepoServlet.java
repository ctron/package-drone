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
import java.util.Optional;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.common.servlet.Handler;
import de.dentrassi.pm.r5.internal.handler.NotFoundHandler;
import de.dentrassi.pm.storage.channel.ChannelArtifactInformation;
import de.dentrassi.pm.storage.channel.ChannelNotFoundException;
import de.dentrassi.pm.storage.channel.ChannelService.By;
import de.dentrassi.pm.storage.channel.ReadableChannel;
import de.dentrassi.pm.storage.channel.servlet.AbstractChannelServiceServlet;
import de.dentrassi.pm.storage.channel.util.DownloadHelper;
import de.dentrassi.pm.storage.web.utils.ChannelCacheHandler;

public abstract class CommonRepoServlet extends AbstractChannelServiceServlet
{

    private static final long serialVersionUID = 1L;

    /**
     * This key points to the main index file which should be served
     */
    private final MetaKey keyToIndex;

    private final ChannelCacheHandler indexHandler;

    public CommonRepoServlet ( final MetaKey keyToIndex )
    {
        this.keyToIndex = keyToIndex;
        this.indexHandler = new ChannelCacheHandler ( this.keyToIndex );
    }

    protected NotFoundHandler resourceNotFound ( final HttpServletRequest req )
    {
        return new NotFoundHandler ( String.format ( "Resource '%s' not found.%n", req.getPathInfo () ) );
    }

    protected boolean process ( final HttpServletRequest req, final HttpServletResponse resp ) throws IOException
    {
        final String path = req.getPathInfo ();

        if ( path == null || path.isEmpty () || "/".equals ( path ) )
        {
            getHelpHandler ().process ( req, resp );
        }

        final String[] toks = req.getPathInfo ().split ( "\\/" );

        if ( toks.length == 2 )
        {
            try
            {
                getService ( req ).access ( By.nameOrId ( toks[1] ), ReadableChannel.class, channel -> {
                    this.indexHandler.process ( channel, req, resp );
                } );
            }
            catch ( final ChannelNotFoundException e )
            {
                new NotFoundHandler ( String.format ( "Channel '%s' not found.", toks[1] ) ).process ( req, resp );
            }
            return true;
        }

        // - URL type #1 : /<base>/<channel>/artifact/<artifactId>
        // - URL type #2 : /<base>/<channel>/artifact/<artifactId>/<artifactName>

        if ( ( toks.length == 4 || toks.length == 5 ) && "artifact".equals ( toks[2] ) )
        {
            try
            {
                getService ( req ).access ( By.nameOrId ( toks[1] ), ReadableChannel.class, channel -> {

                    final Optional<ChannelArtifactInformation> artifact = channel.getArtifact ( toks[3] );
                    if ( !artifact.isPresent () )
                    {
                        new NotFoundHandler ( String.format ( "Artifact '%s' not found.", toks[2] ) ).process ( req, resp );
                    }
                    else
                    {
                        DownloadHelper.streamArtifact ( resp, artifact.get (), Optional.of ( "application/vnd.osgi.bundle" ), true, channel, art -> toks.length == 5 ? toks[4] : art.getName () );
                    }
                } );
            }
            catch ( final ChannelNotFoundException e )
            {
                new NotFoundHandler ( String.format ( "Channel '%s' not found.", toks[1] ) ).process ( req, resp );
            }
            return true;
        }

        return false;
    }

    protected abstract Handler getHelpHandler ();

    @Override
    protected void doGet ( final HttpServletRequest req, final HttpServletResponse resp ) throws ServletException, IOException
    {
        final boolean didProcess = process ( req, resp );

        if ( !didProcess )
        {
            resourceNotFound ( req ).process ( req, resp );;
        }
    }

}
