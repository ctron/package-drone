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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import org.eclipse.packagedrone.repo.manage.system.SitePrefixService;
import org.eclipse.packagedrone.utils.xml.XmlToolsFactory;

public class SitemapServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    private XmlToolsFactory xml;

    private SitePrefixService prefixService;

    private SitemapGenerator generator;

    public void setXml ( final XmlToolsFactory xml )
    {
        this.xml = xml;
    }

    public void setPrefixService ( final SitePrefixService prefixService )
    {
        this.prefixService = prefixService;
    }

    @Override
    public void init () throws ServletException
    {
        super.init ();
        this.generator = new SitemapGenerator ( this.prefixService::getSitePrefix );
    }

    @Override
    public void destroy ()
    {
        super.destroy ();
        this.generator.dispose ();
    }

    @Override
    protected void doGet ( final HttpServletRequest req, final HttpServletResponse resp ) throws ServletException, IOException
    {
        final XMLOutputFactory xof = this.xml.newXMLOutputFactory ();

        resp.setContentType ( "text/xml" );

        try
        {
            this.generator.write ( xof.createXMLStreamWriter ( resp.getWriter () ) );
        }
        catch ( final XMLStreamException e )
        {
            throw new ServletException ( e );
        }
    }
}
