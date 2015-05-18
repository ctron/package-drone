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

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.http.context.ServletContextHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServletContextImpl extends ServletContextHelper
{
    private final static Logger logger = LoggerFactory.getLogger ( ServletContextImpl.class );

    private final List<ResourceProvider> sources = new LinkedList<> ();

    public ServletContextImpl ()
    {
        final BundleContext context = FrameworkUtil.getBundle ( ServletContextImpl.class ).getBundleContext ();

        this.sources.add ( new BundleResourceProvider ( context ) );
        this.sources.add ( new TagDirTracker ( context ) );
        this.sources.add ( new TagLibTracker ( context ) );
    }

    @Override
    public boolean handleSecurity ( final HttpServletRequest request, final HttpServletResponse response ) throws IOException
    {
        // TODO Auto-generated method stub
        return super.handleSecurity ( request, response );
    }

    @Override
    public URL getResource ( final String name )
    {
        if ( logger.isTraceEnabled () )
        {
            logger.trace ( "Getting resource: {}", name );
        }

        final URL result = internalGetResource ( name );

        if ( logger.isTraceEnabled () )
        {
            logger.trace ( "Getting resource: {} -> {}", name, result );
        }

        return result;
    }

    @Override
    public Set<String> getResourcePaths ( final String path )
    {
        if ( logger.isTraceEnabled () )
        {
            logger.trace ( "Getting resource paths: {}", path );
        }

        final Set<String> result = new HashSet<> ();

        internalGetResourcePaths ( path, result );

        if ( logger.isTraceEnabled () )
        {
            logger.trace ( "Getting resource paths: {} -> {}", path, result );
        }

        return result.isEmpty () ? null : result;
    }

    protected URL internalGetResource ( final String name )
    {
        for ( final ResourceProvider provider : this.sources )
        {
            final URL result = provider.getResource ( name );
            if ( result != null )
            {
                return result;
            }
        }

        return null;
    }

    protected void internalGetResourcePaths ( final String path, final Set<String> result )
    {
        for ( final ResourceProvider provider : this.sources )
        {
            final Set<String> providerResult = provider.getPaths ( path );
            if ( providerResult != null )
            {
                result.addAll ( providerResult );
            }
        }
    }

    public void dispose ()
    {
        for ( final ResourceProvider provider : this.sources )
        {
            provider.dispose ();
        }
    }

}
