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
package de.dentrassi.osgi.web.servlet;

import java.io.File;

import javax.servlet.MultipartConfigElement;
import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.eclipse.equinox.jsp.jasper.JspServlet;
import org.ops4j.pax.web.service.WebContainer;
import org.osgi.framework.Bundle;
import org.osgi.service.http.NamespaceException;

/**
 * Register a servlet and add JSP support
 * <p>
 * <em>Note:</em> All JSPs must be located under <code>/WEB-INF/views</code>
 * </p>
 */
public abstract class JspServletInitializer
{
    private WebContainer httpService;

    private DispatcherHttpContext webctx;

    private JspServlet jspServlet;

    private final String alias;

    private Servlet servlet;

    private final Bundle bundle;

    protected abstract Servlet createServlet ();

    public JspServletInitializer ( final String alias, final Bundle bundle )
    {
        this.alias = !alias.endsWith ( "/" ) ? alias : alias.substring ( 0, alias.length () - 1 );
        this.bundle = bundle;
    }

    public void setHttpService ( final WebContainer httpService )
    {
        this.httpService = httpService;
    }

    public void start () throws ServletException, NamespaceException
    {
        this.webctx = Dispatcher.createContext ( this.bundle.getBundleContext () );

        this.servlet = createServlet ();
        this.httpService.registerServlet ( this.alias, this.servlet, null, this.webctx );

        this.jspServlet = new JspServlet ( this.bundle, "/WEB-INF/views", "/WEB-INF/views" );
        this.httpService.registerServlet ( this.jspServlet, new String[] { "*.jsp" }, null, this.webctx );
    }

    public void stop ()
    {
        this.httpService.unregisterServlet ( this.jspServlet );
        this.httpService.unregisterServlet ( this.servlet );

        this.webctx.dispose ();
    }

    public static MultipartConfigElement createMultiPartConfiguration ( final String prefix )
    {
        final String location = System.getProperty ( prefix + ".location", System.getProperty ( "java.io.tmpdir" ) + File.separator + prefix );
        final long maxFileSize = Long.getLong ( prefix + ".maxFileSize", 1 * 1024 * 1024 * 1024 /* 1GB */ );
        final long maxRequestSize = Long.getLong ( prefix + ".maxRequestSize", 1 * 1024 * 1024 * 1024/* 1GB */ );
        final int fileSizeThreshold = Integer.getInteger ( prefix + ".fileSizeThreshold", 10 * 1024 * 1024 /* 10 MB */ );

        return new MultipartConfigElement ( location, maxFileSize, maxRequestSize, fileSizeThreshold );
    }

}
