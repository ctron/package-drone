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
package de.dentrassi.pm.importer.http.web;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import de.dentrassi.osgi.job.AbstractJsonJobFactory;
import de.dentrassi.osgi.job.JobFactoryDescriptor;
import de.dentrassi.osgi.job.JobInstance.Context;
import de.dentrassi.osgi.web.LinkTarget;
import de.dentrassi.pm.importer.http.Configuration;
import de.dentrassi.pm.importer.http.HttpImporter;

public class DownloadTester extends AbstractJsonJobFactory<Configuration, TestResult>
{
    public static final String ID = "de.dentrassi.pm.importer.http.web.tester";

    private static final JobFactoryDescriptor DESCRIPTOR = new JobFactoryDescriptor () {

        @Override
        public LinkTarget getResultTarget ()
        {
            return null;
        }
    };

    public DownloadTester ()
    {
        super ( Configuration.class );
    }

    @Override
    protected TestResult process ( final Context context, final Configuration cfg ) throws Exception
    {
        final TestResult result = new TestResult ();

        final URL url = new URL ( cfg.getUrl () );
        final URLConnection con = url.openConnection ();
        if ( con instanceof HttpURLConnection )
        {
            final HttpURLConnection httpCon = (HttpURLConnection)con;
            httpCon.setRequestMethod ( "HEAD" );
        }

        con.connect ();

        if ( con instanceof HttpURLConnection )
        {
            final HttpURLConnection httpCon = (HttpURLConnection)con;
            result.setReturnCode ( httpCon.getResponseCode () );
            final long length = httpCon.getContentLengthLong ();
            result.setContentLength ( length );
        }

        final String name = HttpImporter.makeName ( cfg, url, con );
        result.setName ( name );

        return result;
    }

    @Override
    protected String makeLabelFromData ( final Configuration data )
    {
        return String.format ( "Test download from: %s", data.getUrl () );
    }

    @Override
    public JobFactoryDescriptor getDescriptor ()
    {
        return DESCRIPTOR;
    }
}
