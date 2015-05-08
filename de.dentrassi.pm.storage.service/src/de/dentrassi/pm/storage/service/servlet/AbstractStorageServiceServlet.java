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

import static de.dentrassi.osgi.web.util.BasicAuthentication.parseAuthorization;

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

import de.dentrassi.osgi.web.util.BasicAuthentication;
import de.dentrassi.pm.storage.Channel;
import de.dentrassi.pm.storage.DeployKey;
import de.dentrassi.pm.storage.service.StorageService;

/**
 * This is an abstract implementation for implementing servlets which require
 * the {@link StorageService}.
 * <p>
 * The servlet ensures that the service methods (GET, POST, ... ) only get
 * called
 * when there is a storage service present. The service can then be fetched
 * using {@link #getService(HttpServletRequest)}.
 * </p<
 */
public abstract class AbstractStorageServiceServlet extends HttpServlet
{
    private final static Logger logger = LoggerFactory.getLogger ( AbstractStorageServiceServlet.class );

    private static final long serialVersionUID = 1L;

    private static final String ATTR_STORAGE_SERVICE = AbstractStorageServiceServlet.class.getName () + ".storageService";

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

    protected StorageService getService ( final HttpServletRequest request )
    {
        return (StorageService)request.getAttribute ( ATTR_STORAGE_SERVICE );
    }

    @Override
    protected void service ( final HttpServletRequest request, final HttpServletResponse response ) throws ServletException, IOException
    {
        final StorageService service = this.tracker.getService ();

        if ( service == null )
        {
            handleNoService ( request, response );
        }
        else
        {
            request.setAttribute ( ATTR_STORAGE_SERVICE, service );
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

    /**
     * Authenticate the request is authenticated against the deploy keys
     * <p>
     * If the request could not be authenticated a basic authentication request
     * is sent back and the {@link HttpServletResponse} will be committed.
     * </p>
     *
     * @param channel
     *            the channel to act on
     * @param request
     *            the request
     * @param response
     *            the response
     * @return <code>true</code> if the request was not authenticated and the
     *         response got committed
     * @throws IOException
     *             in case on a IO error
     */
    protected boolean authenticate ( final Channel channel, final HttpServletRequest request, final HttpServletResponse response ) throws IOException
    {
        if ( isAuthenticated ( channel, request ) )
        {
            return true;
        }

        BasicAuthentication.request ( response, "channel-" + channel.getId (), "Please authenticate" );

        return false;
    }

    /**
     * Simply test if the request is authenticated against the channels deploy
     * keys
     *
     * @param channel
     *            the channel to act on
     * @param request
     *            the request
     * @return <code>true</code> if the request could be authenticated against
     *         the channels deploy keys, <code>false</code> otherwise
     */
    protected boolean isAuthenticated ( final Channel channel, final HttpServletRequest request )
    {
        final String[] authToks = parseAuthorization ( request );

        if ( authToks == null )
        {
            return false;
        }

        if ( !authToks[0].equals ( "deploy" ) )
        {
            return false;
        }

        final String deployKey = authToks[1];

        logger.debug ( "Deploy key: '{}'", deployKey );

        for ( final DeployKey key : channel.getAllDeployKeys () )
        {
            if ( key.getKey ().equals ( deployKey ) )
            {
                return true;
            }
        }

        return false;
    }

}
