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
package org.eclipse.packagedrone.repo.web.sitemap.internal;

import static java.util.Optional.ofNullable;

import java.io.IOException;
import java.util.function.Supplier;

import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLOutputFactory;

import org.eclipse.packagedrone.repo.web.sitemap.SitemapGenerator;
import org.eclipse.packagedrone.repo.web.sitemap.SitemapIndexWriter;
import org.eclipse.packagedrone.repo.web.sitemap.UrlSetWriter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.ServiceTracker;

public class SitemapProcessor
{
    private final Supplier<String> prefixSupplier;

    private final String sitemapUrl;

    private final XMLOutputFactory outputFactory;

    private final ServiceTracker<SitemapGenerator, SitemapGenerator> tracker;

    public SitemapProcessor ( final Supplier<String> prefixSupplier, final String sitemapUrl, final XMLOutputFactory outputFactory )
    {
        this.prefixSupplier = prefixSupplier;
        this.sitemapUrl = sitemapUrl;
        this.outputFactory = outputFactory;

        this.tracker = new ServiceTracker<> ( FrameworkUtil.getBundle ( SitemapProcessor.class ).getBundleContext (), SitemapGenerator.class, null );
        this.tracker.open ();
    }

    public void dispose ()
    {
        this.tracker.close ();
    }

    public boolean process ( final HttpServletResponse response, final String path ) throws IOException
    {
        if ( path == null || path.isEmpty () )
        {
            processRoot ( response );
            return true;
        }
        else
        {
            return processSub ( response, path );
        }
    }

    private void processRoot ( final HttpServletResponse response ) throws IOException
    {
        final String prefix = ofNullable ( this.prefixSupplier.get () ).orElse ( "http://localhost" ) + this.sitemapUrl;

        response.setContentType ( "text/xml" );

        try ( SitemapIndexWriter writer = new SitemapIndexWriter ( response.getWriter (), prefix, this.outputFactory ) )
        {
            for ( final SitemapGenerator generator : this.tracker.getTracked ().values () )
            {
                generator.gather ( writer );
            }
        }
    }

    @SuppressWarnings ( "resource" )
    private boolean processSub ( final HttpServletResponse response, final String path ) throws IOException
    {
        final String prefix = ofNullable ( this.prefixSupplier.get () ).orElse ( "http://localhost" );

        // we must not close the writer, since we don't know if we will really process the request
        final UrlSetWriter writer = new UrlSetWriter ( response.getWriter (), prefix, this.outputFactory );

        for ( final SitemapGenerator generator : this.tracker.getTracked ().values () )
        {
            if ( generator.render ( path, writer ) )
            {
                writer.finish ();
                return true;
            }
        }

        return false;
    }

}
