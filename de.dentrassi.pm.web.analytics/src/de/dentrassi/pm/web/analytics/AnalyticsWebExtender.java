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
package de.dentrassi.pm.web.analytics;

import static de.dentrassi.pm.web.analytics.Constants.KEY_ANONYMIZE_IP;
import static de.dentrassi.pm.web.analytics.Constants.KEY_FORCE_SSL;
import static de.dentrassi.pm.web.analytics.Constants.KEY_TRACKING_ID;
import static de.dentrassi.pm.web.analytics.Constants.NAMESPACE;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import de.dentrassi.osgi.web.extender.WebExtender;
import de.dentrassi.pm.core.CoreService;

public class AnalyticsWebExtender implements WebExtender
{
    private CoreService service;

    private static final String CODE_START = "<script>\n(function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){(i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)})(window,document,'script','//www.google-analytics.com/analytics.js','ga');\n\n";

    private static final String CODE_END = "\n</script>\n";

    public void setService ( final CoreService service )
    {
        this.service = service;
    }

    @Override
    public void processHead ( final HttpServletRequest request, final Writer writer ) throws IOException
    {
        final Map<String, String> data = this.service.getCoreNamespacePlainProperties ( NAMESPACE, KEY_TRACKING_ID, KEY_ANONYMIZE_IP, KEY_FORCE_SSL );

        final String trackingId = data.get ( KEY_TRACKING_ID );
        if ( trackingId == null || trackingId.isEmpty () )
        {
            return;
        }

        final boolean anon = Boolean.parseBoolean ( data.get ( KEY_ANONYMIZE_IP ) );
        final boolean forceSsl = Boolean.parseBoolean ( data.get ( KEY_FORCE_SSL ) );

        final StringBuilder sb = new StringBuilder ( CODE_START );

        sb.append ( "ga('create', '" ).append ( trackingId ).append ( "', 'auto');\n" );
        if ( anon )
        {
            sb.append ( "ga('set', 'anonymizeIp', true);\n" );
        }
        if ( forceSsl )
        {
            sb.append ( "ga('set', 'forceSSL', true);\n" );
        }
        sb.append ( "ga('send', 'pageview');\n" );

        sb.append ( CODE_END );

        writer.write ( sb.toString () );
    }
}
