/*******************************************************************************
 * Copyright (c) 2015 Jens Reimann.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jens Reimann - initial API and implementation
 *******************************************************************************/
package de.dentrassi.pm.deb;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.ServiceTracker;

import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.common.MetaKeys;
import de.dentrassi.pm.deb.servlet.CompDirGenerator;
import de.dentrassi.pm.deb.servlet.DistDirGenerator;
import de.dentrassi.pm.deb.servlet.Helper;
import de.dentrassi.pm.deb.servlet.TypeDirGenerator;
import de.dentrassi.pm.storage.Channel;
import de.dentrassi.pm.storage.service.StorageService;

public class AptServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    private ServiceTracker<StorageService, StorageService> tracker;

    @Override
    public void init () throws ServletException
    {
        super.init ();

        final BundleContext context = FrameworkUtil.getBundle ( AptServlet.class ).getBundleContext ();
        this.tracker = new ServiceTracker<> ( context, StorageService.class, null );
        this.tracker.open ();
    }

    @Override
    public void destroy ()
    {
        this.tracker.close ();
        super.destroy ();
    }

    private static final Pattern POOL_PATTERN = Pattern.compile ( "pool/(?<id>[^/]+)/(?<name>.*)" );

    @Override
    protected void doGet ( final HttpServletRequest request, final HttpServletResponse response ) throws ServletException, IOException
    {
        final StorageService service = this.tracker.getService ();
        if ( service == null )
        {
            response.setStatus ( HttpServletResponse.SC_SERVICE_UNAVAILABLE );
            response.getWriter ().write ( "Service unavailable" );
            return;
        }

        String path = request.getPathInfo ();

        // strip of leading slash
        path = path.replaceAll ( "^/+", "" );
        path = path.replaceAll ( "/+$", "" );

        final String[] toks = path.split ( "/", 2 );

        if ( path.isEmpty () || toks.length == 0 )
        {
            response.setContentType ( "text/plain" );
            response.getWriter ().write ( "PackageInformation Drone - APT repository adapter" );
            return;
        }

        final String channelId = toks[0];
        final Channel channel = service.getChannelWithAlias ( channelId );

        if ( channel == null )
        {
            response.setStatus ( HttpServletResponse.SC_NOT_FOUND );
            response.getWriter ().format ( "Channel '%s' not found", channelId );
            return;
        }

        final String channelPath = toks.length > 1 ? toks[1] : null;

        if ( channelPath == null || channelPath.isEmpty () )
        {
            if ( !request.getPathInfo ().endsWith ( "/" ) )
            {
                response.sendRedirect ( request.getContextPath () + request.getPathInfo () + "/" );
                return;
            }

            final Map<String, Object> model = new HashMap<> ();
            model.put ( "name", channel.getName () );
            model.put ( "id", channel.getId () );
            Helper.render ( response, AptServlet.class.getResource ( "content/index.html" ), model );
            return;
        }

        if ( channelPath.equals ( "pool" ) )
        {
            new PoolHandler ( service, "", "" ).process ( response );
            return;
        }

        final Matcher m = POOL_PATTERN.matcher ( channelPath );
        if ( m.matches () )
        {
            new PoolHandler ( service, m.group ( "id" ), m.group ( "name" ) ).process ( response );
            return;
        }

        final ChannelConfiguration cfg = new ChannelConfiguration ();
        try
        {
            MetaKeys.bind ( cfg, channel.getMetaData () );
        }
        catch ( final Exception e )
        {
            // do nothing
        }

        if ( cfg == null || !cfg.isValid () )
        {
            response.setStatus ( HttpServletResponse.SC_SERVICE_UNAVAILABLE );
            response.getWriter ().format ( "APT configuration not found or not valid. Please ensure the 'APT' aspect is added to this channel and the configuration is valid." );
            return;
        }
        if ( channelPath.equals ( "dists" ) )
        {
            if ( !request.getPathInfo ().endsWith ( "/" ) )
            {
                response.sendRedirect ( request.getContextPath () + request.getPathInfo () + "/" );
                return;
            }

            final Map<String, Object> model = new HashMap<> ();
            model.put ( "distribution", cfg.getDistribution () );
            model.put ( "name", channel.getName () );
            model.put ( "id", channel.getId () );
            Helper.render ( response, AptServlet.class.getResource ( "content/dists.html" ), model );
            return;
        }

        final Handler handler = makeHandler ( request, channel, channelPath, cfg );
        if ( handler == null )
        {
            response.setStatus ( HttpServletResponse.SC_NOT_FOUND );
            response.getWriter ().format ( "Unable to handle request for '%s'", request.getPathInfo () );
        }
        else
        {
            handler.process ( response );
        }
    }

    private Handler makeHandler ( final HttpServletRequest request, final Channel channel, final String channelPath, final ChannelConfiguration cfg )
    {
        final Map<String, Object> model = new HashMap<> ();
        model.put ( "distribution", cfg.getDistribution () );
        model.put ( "name", channel.getName () );
        model.put ( "id", channel.getId () );

        final String toks[] = channelPath.split ( "/" );

        if ( toks.length == 2 && "dists".equals ( toks[0] ) && cfg.getDistribution ().equals ( toks[1] ) )
        {
            if ( !request.getPathInfo ().endsWith ( "/" ) )
            {
                return new RedirectHandler ( request );
            }

            model.put ( "dir", new DistDirGenerator ( cfg ) );
            return new ContentHandler ( AptServlet.class.getResource ( "content/dist-index.html" ), model );
        }

        if ( toks.length == 3 && "dists".equals ( toks[0] ) && cfg.getDistribution ().equals ( toks[1] ) )
        {
            final String component = toks[2];

            switch ( component )
            {
                case "Release":
                    return new MetaDataHandler ( channel.getMetaData (), new MetaKey ( "apt", String.format ( "dists/%s/Release", cfg.getDistribution () ) ), "text/plain" );
            }

            if ( !cfg.getComponents ().contains ( component ) )
            {
                return null;
            }

            if ( !request.getPathInfo ().endsWith ( "/" ) )
            {
                return new RedirectHandler ( request );
            }

            model.put ( "component", component );
            model.put ( "dir", new CompDirGenerator ( cfg ) );
            return new ContentHandler ( AptServlet.class.getResource ( "content/comp-index.html" ), model );
        }

        if ( toks.length == 4 && "dists".equals ( toks[0] ) && cfg.getDistribution ().equals ( toks[1] ) )
        {
            if ( !request.getPathInfo ().endsWith ( "/" ) )
            {
                return new RedirectHandler ( request );
            }

            final String file = toks[3];
            if ( "source".equals ( file ) )
            {
                model.put ( "dir", new TypeDirGenerator ( cfg ) );
                return new ContentHandler ( AptServlet.class.getResource ( "content/type-index.html" ), model );
            }

            for ( final String arch : cfg.getArchitectures () )
            {
                if ( file.equals ( "binary-" + arch ) )
                {
                    model.put ( "dir", new TypeDirGenerator ( cfg ) );
                    return new ContentHandler ( AptServlet.class.getResource ( "content/type-index.html" ), model );
                }
            }
        }

        if ( toks.length == 5 && "dists".equals ( toks[0] ) && cfg.getDistribution ().equals ( toks[1] ) )
        {
            final String component = toks[2];
            final String type = toks[3];
            final String file = toks[4];
            switch ( file )
            {
                case "Release":
                case "Packages":
                    return new MetaDataHandler ( channel.getMetaData (), new MetaKey ( "apt", String.format ( "dists/%s/%s/%s/%s", cfg.getDistribution (), component, type, file ) ), "text/plain" );
                case "Packages.gz":
                    return new MetaDataHandler ( channel.getMetaData (), new MetaKey ( "apt", String.format ( "dists/%s/%s/%s/%s", cfg.getDistribution (), component, type, file ) ), "application/x-gzip" );
                case "Packages.bz2":
                    return new MetaDataHandler ( channel.getMetaData (), new MetaKey ( "apt", String.format ( "dists/%s/%s/%s/%s", cfg.getDistribution (), component, type, file ) ), "application/x-bzip2" );
            }
        }

        return null;
    }
}
