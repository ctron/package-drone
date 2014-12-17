/*******************************************************************************
 * Copyright (c) 2014 Jens Reimann.
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

import de.dentrassi.pm.storage.Channel;

public class IndexHandler implements Handler
{

    private final Channel channel;

    public IndexHandler ( final Channel channel )
    {
        this.channel = channel;
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
        w.format ( "<title>P2 repository for channel: %s</title>", this.channel.getId () );
        w.println ( "</head>" );
        w.println ( "<body>" );

        w.format ( "<header><h1>P2 repository for channel: %s</h1></header>", this.channel.getId () );

        w.println ( "<ul>" );
        w.println ( "<li><a href=\"content.xml\">content.xml</a></li>" );
        w.println ( "<li><a href=\"artifacts.xml\">artifacts.xml</a></li>" );
        w.println ( "<li><a href=\"p2.index\">p2.index</a></li>" );
        w.println ( "</ul>" );

        w.println ( "</body>" );
        w.println ( "</html>" );
    }
}
