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
package de.dentrassi.osgi.web.servlet;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletRegistration.Dynamic;

import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.WebAppContext;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dentrassi.osgi.web.DispatcherServlet;

public class ContextImpl extends WebAppContext
{
    private final static Logger logger = LoggerFactory.getLogger ( ContextImpl.class );

    private final BundleContext context;

    private final TagDirTracker tagdirTracker;

    private final TagLibTracker taglibTracker;

    private FilterTracker filterTracker;

    public ContextImpl ()
    {
        this.context = FrameworkUtil.getBundle ( ContextImpl.class ).getBundleContext ();
        this.tagdirTracker = new TagDirTracker ( this.context );
        this.taglibTracker = new TagLibTracker ( this.context );
    }

    @Override
    public void preConfigure () throws Exception
    {
        super.preConfigure ();

        final ServletHolder holder = addServlet ( DispatcherServlet.class, "/" );

        final Dynamic reg = holder.getRegistration ();

        final long maxLen = Long.getLong ( "drone.web.maxRequestBytes", /* 1GB */1024 * 1024 * 1024 );
        final int fileThreshold = Integer.getInteger ( "drone.web.fileThresholdBytes", /* 1MB */1024 * 1024 );

        reg.setMultipartConfig ( new MultipartConfigElement ( "", maxLen, maxLen, fileThreshold ) );

        this.filterTracker = new FilterTracker ( this.context, getServletContext () );

        // filter

        final javax.servlet.FilterRegistration.Dynamic filter = getServletContext ().addFilter ( "filterTracker", this.filterTracker );
        filter.addMappingForUrlPatterns ( null, false, "/*" );
    }

    @Override
    protected void doStart () throws Exception
    {
        this.tagdirTracker.open ();
        this.taglibTracker.open ();
        super.doStart ();
    }

    @Override
    public Set<String> getResourcePaths ( final String name )
    {
        if ( logger.isTraceEnabled () )
        {
            logger.trace ( "Getting resource paths: {}", name );
        }

        final Set<String> resources = new HashSet<String> ();

        {
            final Set<String> tldResources = this.tagdirTracker.getUrls ( name );
            if ( tldResources != null )
            {
                resources.addAll ( tldResources );
            }
        }

        if ( name.equals ( "/WEB-INF/" ) )
        {
            // add all taglibs
            resources.addAll ( this.taglibTracker.getAllEntries () );
        }

        resources.addAll ( super.getResourcePaths ( name ) );

        if ( logger.isTraceEnabled () )
        {
            logger.trace ( "Result: {}", resources );
        }

        return resources;
    }

    @Override
    protected void doStop () throws Exception
    {
        super.doStop ();
        this.tagdirTracker.close ();
        this.taglibTracker.close ();

        // clean up the cache

        synchronized ( this.resourceCache )
        {
            for ( final Resource r : this.resourceCache.values () )
            {
                r.close ();
            }
            this.resourceCache.clear ();
        }
    }

    private final Map<String, Resource> resourceCache = new HashMap<> ();

    @Override
    public Resource getResource ( final String name ) throws MalformedURLException
    {
        if ( logger.isTraceEnabled () )
        {
            logger.trace ( "Getting resource: {}", name );
        }

        Resource resource;
        synchronized ( this.resourceCache )
        {
            resource = this.resourceCache.get ( name );
            if ( resource != null )
            {
                // we found something useful in the cache
                return resource;
            }
        }

        // try to create the resource from internal sources

        resource = getInternalResource ( name );

        // process the result

        if ( resource != null )
        {
            // we created something
            synchronized ( this.resourceCache )
            {
                if ( !this.resourceCache.containsKey ( name ) )
                {
                    // nobody else put somethere here in the meantime
                    this.resourceCache.put ( name, resource );
                    return resource;
                }
                else
                {
                    resource.close (); // close what we just created
                    resource = this.resourceCache.get ( name );
                    if ( resource != null )
                    {
                        // return what somebody else put here
                        return resource;
                    }
                }
            }
        }

        // we now that we cannot provide the resource, pass on to super

        return super.getResource ( name );
    }

    /**
     * Get a resource from internal source this implementation controls
     *
     * @param name
     *            the name or the resource
     * @return the resource, or <code>null</code> if none was found
     * @throws MalformedURLException
     *             if the resource name was considered invalid
     */
    private Resource getInternalResource ( final String name ) throws MalformedURLException
    {
        if ( name.startsWith ( "/bundle/" ) )
        {
            return getAsBundleResource ( name );
        }

        Resource result;

        result = this.tagdirTracker.getTagLib ( name );
        if ( result != null )
        {
            logger.trace ( "Return as tagdir resource - {}", name );
            return result;
        }

        if ( name.startsWith ( "/WEB-INF/" ) )
        {
            result = this.taglibTracker.getTagLib ( name.substring ( "/WEB-INF/".length () ) );
            if ( result != null )
            {
                logger.trace ( "Return as taglib resource - {}", name );
                return result;
            }
        }

        return null;
    }

    private Resource getAsBundleResource ( final String name ) throws MalformedURLException
    {
        final String toks[] = name.split ( "/", 4 );

        if ( toks.length != 4 )
        {
            throw new MalformedURLException ();
        }

        if ( !toks[1].equals ( "bundle" ) )
        {
            return null;
        }

        final long bundleId;
        try
        {
            bundleId = Long.parseLong ( toks[2] );
        }
        catch ( final NumberFormatException e )
        {
            logger.debug ( "Failed to parse bundle id", e );
            throw new MalformedURLException ( String.format ( "Invalid bundle Id: %s", toks[2] ) );
        }

        final Bundle bundle = findBundle ( bundleId );
        logger.trace ( "Target bundle: {}", bundle );
        if ( bundle == null )
        {
            return null;
        }

        final URL result = bundle.getEntry ( toks[3] );
        logger.trace ( "Resource entry ({}): {}", toks[3], result );
        if ( result == null )
        {
            return null;
        }

        logger.debug ( "Requesting resource: {}", result );

        return Resource.newResource ( result );
    }

    private Bundle findBundle ( final long bundleId )
    {
        return this.context.getBundle ( bundleId );
    }
}
