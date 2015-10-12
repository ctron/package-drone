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

import java.net.URI;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.eclipse.packagedrone.repo.web.sitemap.ChangeFrequency;
import org.eclipse.packagedrone.repo.web.sitemap.SitemapContext;
import org.eclipse.packagedrone.repo.web.sitemap.SitemapExtender;
import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.ServiceTracker;

public class SitemapGenerator
{
    private static final String NS = "http://www.sitemaps.org/schemas/sitemap/0.9";

    private static final String NL = "\n";

    private static final String IN = "  ";

    private static final String IN2 = IN + IN;

    private static DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern ( "yyyy-MM-dd'T'hh:mm:ssXXX", Locale.ROOT );

    private final ServiceTracker<SitemapExtender, SitemapExtender> tracker;

    private final Supplier<String> prefixSupplier;

    public SitemapGenerator ( final Supplier<String> prefixSupplier )
    {
        this.prefixSupplier = prefixSupplier;
        this.tracker = new ServiceTracker<> ( FrameworkUtil.getBundle ( SitemapGenerator.class ).getBundleContext (), SitemapExtender.class, null );
        this.tracker.open ();
    }

    public void write ( final XMLStreamWriter out ) throws XMLStreamException
    {
        out.writeStartDocument ( "UTF-8", "1.0" );
        out.writeCharacters ( NL );

        out.writeStartElement ( "urlset" );
        out.writeDefaultNamespace ( NS );
        out.writeCharacters ( NL );

        final String prefix = Optional.ofNullable ( this.prefixSupplier.get () ).orElse ( "http://localhost" );

        final SitemapContext ctx = new SitemapContext () {

            @Override
            public void addLocation ( String localUrl, final Optional<Instant> lastModification, final Optional<ChangeFrequency> changeFrequency, final Optional<Double> priority )
            {
                if ( !localUrl.startsWith ( "/" ) )
                {
                    localUrl = "/" + localUrl;
                }

                try
                {
                    out.writeCharacters ( IN );
                    out.writeStartElement ( "url" );
                    out.writeCharacters ( NL );

                    writeTag ( out, "loc", URI.create ( prefix + localUrl ).toASCIIString () );

                    if ( lastModification.isPresent () )
                    {
                        writeTag ( out, "lastmod", FORMAT.format ( lastModification.get ().atZone ( ZoneId.systemDefault () ) ) );
                    }
                    if ( changeFrequency.isPresent () )
                    {
                        writeTag ( out, "changefreq", changeFrequency.get ().getValue () );
                    }
                    if ( priority.isPresent () )
                    {
                        final double value = Math.min ( Math.max ( 0.0, priority.get () ), 1.0 );
                        writeTag ( out, "priority", String.format ( "%.1f", value ) );
                    }

                    out.writeCharacters ( IN );
                    out.writeEndElement (); // url
                }
                catch ( final XMLStreamException e )
                {
                    // silently ignore
                }
            }
        };

        for ( final SitemapExtender extender : this.tracker.getTracked ().values () )
        {
            extender.extend ( ctx );;
        }

        out.writeEndElement ();

        out.writeEndDocument ();
    }

    public void dispose ()
    {
        this.tracker.close ();
    }

    private void writeTag ( final XMLStreamWriter out, final String tagName, final String value ) throws XMLStreamException
    {
        out.writeCharacters ( IN2 );
        out.writeStartElement ( tagName );
        out.writeCharacters ( value );
        out.writeEndElement (); // loc
        out.writeCharacters ( NL );
    }
}
