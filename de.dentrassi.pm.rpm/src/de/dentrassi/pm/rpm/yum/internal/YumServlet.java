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
package de.dentrassi.pm.rpm.yum.internal;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.dentrassi.pm.VersionInformation;
import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.rpm.Constants;
import de.dentrassi.pm.storage.Artifact;
import de.dentrassi.pm.storage.CacheEntryInformation;
import de.dentrassi.pm.storage.Channel;
import de.dentrassi.pm.storage.service.servlet.AbstractStorageServiceServlet;
import de.dentrassi.pm.storage.service.util.DownloadHelper;
import de.dentrassi.pm.storage.web.utils.ChannelCacheHandler;

public class YumServlet extends AbstractStorageServiceServlet
{
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet ( final HttpServletRequest request, final HttpServletResponse response ) throws ServletException, IOException
    {
        String path = request.getServletPath ();

        if ( path != null && path.startsWith ( "/" ) )
        {
            path = path.substring ( 1 );
        }

        if ( path == null || path.isEmpty () )
        {
            handleWelcome ( request, response );
            return;
        }

        final String[] segs = path.split ( "/", 2 );
        if ( segs.length <= 0 )
        {
            handleWelcome ( request, response );
            return;
        }

        final String channelId = segs[0];
        final String remPath = segs.length > 1 ? segs[1] : null;

        final Channel channel = getService ( request ).getChannelWithAlias ( channelId );
        if ( channel == null )
        {
            handleMessage ( response, HttpServletResponse.SC_NOT_FOUND, String.format ( "Channel '%s' could not be found", channelId ) );
            return;
        }

        if ( handleChannel ( channel, remPath, request, response ) )
        {
            return;
        }

        handleNotFound ( request, response, request.getRequestURI () );
    }

    private boolean handleChannel ( final Channel channel, final String remPath, final HttpServletRequest request, final HttpServletResponse response ) throws IOException, ServletException
    {
        if ( remPath == null || remPath.isEmpty () )
        {
            if ( !request.getServletPath ().endsWith ( "/" ) )
            {
                response.setStatus ( HttpServletResponse.SC_MOVED_PERMANENTLY );
                response.sendRedirect ( request.getRequestURI () + "/" );
                return true;
            }

            request.setAttribute ( "channel", channel );
            viewJsp ( request, response, "channel.jsp" );
            return true;
        }

        // handle pool

        // FIXME: handle pool
        if ( remPath.startsWith ( "pool/" ) )
        {
            handlePool ( channel, remPath, request, response );
            return true;
        }

        // handle repo data

        if ( "repodata".equals ( remPath ) || "repodata/".equals ( remPath ) )
        {
            if ( !request.getServletPath ().endsWith ( "/" ) )
            {
                response.setStatus ( HttpServletResponse.SC_MOVED_PERMANENTLY );
                response.sendRedirect ( request.getRequestURI () + "/" );
                return true;
            }

            request.setAttribute ( "channel", channel );

            final List<CacheEntryInformation> files = channel.getAllCacheEntries ().stream ().filter ( ce -> ce.getKey ().getNamespace ().equals ( Constants.YUM_ASPECT_ID ) && ce.getName ().startsWith ( "repodata/" ) ).collect ( Collectors.toList () );
            request.setAttribute ( "entries", files );

            viewJsp ( request, response, "repodata.jsp" );
            return true;
        }

        if ( remPath.startsWith ( "repodata/" ) )
        {
            new ChannelCacheHandler ( new MetaKey ( Constants.YUM_ASPECT_ID, remPath ) ).process ( channel, request, response );
            return true;
        }

        return false;
    }

    private void handlePool ( final Channel channel, final String remPath, final HttpServletRequest request, final HttpServletResponse response ) throws IOException
    {
        final String[] segs = remPath.split ( "/" );
        if ( segs.length < 3 )
        {
            handleNotFound ( request, response, request.getRequestURI () );
            return;
        }

        final Artifact artifact = channel.getArtifact ( segs[1] );
        if ( artifact == null )
        {
            handleNotFound ( request, response, request.getRequestURI () );
            return;
        }

        final Optional<String> name = segs.length > 2 ? Optional.of ( segs[segs.length - 1] ) : Optional.empty ();

        DownloadHelper.streamArtifact ( response, artifact, null, true, art -> name.orElse ( art.getName () ) );
    }

    private void viewJsp ( final HttpServletRequest request, final HttpServletResponse response, final String viewName ) throws ServletException, IOException
    {
        request.setAttribute ( "version", VersionInformation.VERSION );
        request.getRequestDispatcher ( "/WEB-INF/views/" + viewName ).forward ( request, response );
    }

    private void handleNotFound ( final HttpServletRequest request, final HttpServletResponse response, final String resource ) throws IOException
    {
        handleMessage ( response, HttpServletResponse.SC_NOT_FOUND, String.format ( "Resource '%s' could not be found", resource ) );
    }

    private void handleMessage ( final HttpServletResponse response, final int status, final String message ) throws IOException
    {
        response.setContentType ( "text/plain" );
        response.setStatus ( status );
        response.getWriter ().write ( message );
    }

    private void handleWelcome ( final HttpServletRequest request, final HttpServletResponse response ) throws IOException, ServletException
    {
        viewJsp ( request, response, "index.jsp" );
    }
}
