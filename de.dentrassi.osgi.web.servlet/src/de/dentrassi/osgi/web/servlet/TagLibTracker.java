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
package de.dentrassi.osgi.web.servlet;

import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jetty.util.resource.Resource;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dentrassi.osgi.utils.AttributedValue;
import de.dentrassi.osgi.utils.Headers;

public class TagLibTracker
{
    private final static Logger logger = LoggerFactory.getLogger ( TagLibTracker.class );

    private final BundleTracker<TagLibInfo> bundleTracker;

    private final BundleTrackerCustomizer<TagLibInfo> customizer = new BundleTrackerCustomizer<TagLibInfo> () {

        @Override
        public TagLibInfo addingBundle ( final Bundle bundle, final BundleEvent event )
        {
            return createTagLibInfo ( bundle );
        }

        @Override
        public void modifiedBundle ( final Bundle bundle, final BundleEvent event, final TagLibInfo object )
        {
        }

        @Override
        public void removedBundle ( final Bundle bundle, final BundleEvent event, final TagLibInfo object )
        {
        }
    };

    private static class TagLibInfo
    {
        private final Map<String, URL> tlds;

        public TagLibInfo ( final Map<String, URL> tlds )
        {
            this.tlds = tlds;
        }

        @Override
        public String toString ()
        {
            return String.format ( "[TagLib - tlds: %s]", this.tlds );
        }

        public Set<String> getEntries ()
        {
            return this.tlds.keySet ();
        }

        public Map<String, URL> getTlds ()
        {
            return this.tlds;
        }
    }

    public TagLibTracker ( final BundleContext context )
    {
        this.bundleTracker = new BundleTracker<> ( context, Bundle.RESOLVED | Bundle.ACTIVE, this.customizer );
    }

    protected TagLibInfo createTagLibInfo ( final Bundle bundle )
    {
        logger.trace ( "Checking for tag lib directories: {}", bundle );

        final String tldHeader = bundle.getHeaders ().get ( "Web-Export-Taglib" );
        if ( tldHeader != null )
        {
            final Map<String, URL> tlds = new HashMap<> ();
            final List<AttributedValue> list = Headers.parseList ( tldHeader );
            for ( final AttributedValue av : list )
            {
                String tld = av.getValue ();
                if ( !tld.startsWith ( "/" ) )
                {
                    tld = "/" + tld;
                }
                final URL entry = bundle.getEntry ( tld );
                if ( entry == null )
                {
                    logger.warn ( "Failed to resolve - {}", tld );
                }
                else
                {
                    final String key = makeKey ( tld );
                    logger.info ( "Found tag lib  {} in bundle {} (as '{}')", tld, bundle, key );
                    tlds.put ( key, entry );
                }
            }

            final TagLibInfo result = new TagLibInfo ( tlds );
            return result;
        }
        return null;
    }

    private String makeKey ( final String tld )
    {
        final String[] toks = tld.split ( "\\/" );
        return toks[toks.length - 1];
    }

    public void open ()
    {
        this.bundleTracker.open ();
    }

    public void close ()
    {
        this.bundleTracker.close ();
    }

    public Collection<? extends String> getAllEntries ()
    {
        final Set<String> result = new HashSet<> ();

        for ( final TagLibInfo tli : this.bundleTracker.getTracked ().values () )
        {
            for ( final String name : tli.getEntries () )
            {
                result.add ( "/WEB-INF/" + name );
            }
        }

        return result;
    }

    public Resource getTagLib ( final String name )
    {
        for ( final TagLibInfo tli : this.bundleTracker.getTracked ().values () )
        {
            final URL result = tli.getTlds ().get ( name );
            if ( result != null )
            {
                return Resource.newResource ( result );
            }
        }

        return null;
    }
}
