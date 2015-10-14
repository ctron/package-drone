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

import java.io.IOException;
import java.io.Writer;
import java.util.Optional;
import java.util.function.Supplier;

import javax.xml.stream.XMLOutputFactory;

import org.eclipse.packagedrone.repo.web.sitemap.SitemapExtender;
import org.eclipse.packagedrone.repo.web.sitemap.UrlSetWriter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.ServiceTracker;

public class SitemapGenerator
{
    private final ServiceTracker<SitemapExtender, SitemapExtender> tracker;

    private final Supplier<String> prefixSupplier;

    private final XMLOutputFactory outputFactory;

    public SitemapGenerator ( final Supplier<String> prefixSupplier, final XMLOutputFactory outputFactory )
    {
        this.prefixSupplier = prefixSupplier;
        this.outputFactory = outputFactory;

        this.tracker = new ServiceTracker<> ( FrameworkUtil.getBundle ( SitemapGenerator.class ).getBundleContext (), SitemapExtender.class, null );
        this.tracker.open ();
    }

    public void dispose ()
    {
        this.tracker.close ();
    }

    public void write ( final Writer writer ) throws IOException
    {
        final String prefix = Optional.ofNullable ( this.prefixSupplier.get () ).orElse ( "http://localhost" );
        try ( UrlSetWriter urlSetWriter = new UrlSetWriter ( writer, prefix, this.outputFactory ) )
        {
            for ( final SitemapExtender extender : this.tracker.getTracked ().values () )
            {
                extender.extend ( urlSetWriter );
            }
        }
    }

}
