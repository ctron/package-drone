/*******************************************************************************
 * Copyright (c) 2014, 2015 Jens Reimann.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jens Reimann - initial API and implementation
 *******************************************************************************/
package de.dentrassi.pm.p2.servlet;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.html.HtmlEscapers;

import de.dentrassi.pm.storage.Channel;

public class IndexHandler extends AbstractChannelHandler
{

    public IndexHandler ( final Channel channel )
    {
        super ( channel );
    }

    @Override
    public void prepare () throws Exception
    {
    }

    @Override
    public void process ( final HttpServletRequest req, final HttpServletResponse resp ) throws Exception
    {
        resp.setContentType ( "text/html" );
        final PrintWriter w = resp.getWriter ();

        w.println ( "<!DOCTYPE html>" );
        w.println ( "<html>" );
        w.println ( "<head>" );
        w.println ( "<meta charset=\"UTF-8\">" );
        w.format ( "<title>P2 repository for channel: %s</title>", makeTitle () );
        w.println ( "</head>" );
        w.println ( "<body>" );

        w.format ( "<header><h1>P2 repository for channel: %s</h1>", makeTitle () );
        {
            final String title = getTitle ();
            if ( title != null && !title.isEmpty () )
            {
                w.format ( "<h2>%s<h2>", HtmlEscapers.htmlEscaper ().escape ( title ) );
            }
        }
        w.println ( "</header>" );

        w.println ( "<ul>" );
        w.println ( "<li><a href=\"content.xml\">content.xml</a></li>" );
        w.println ( "<li><a href=\"artifacts.xml\">artifacts.xml</a></li>" );
        w.println ( "<li><a href=\"p2.index\">p2.index</a></li>" );
        w.println ( "</ul>" );

        w.println ( "</body>" );
        w.println ( "</html>" );
    }
}
