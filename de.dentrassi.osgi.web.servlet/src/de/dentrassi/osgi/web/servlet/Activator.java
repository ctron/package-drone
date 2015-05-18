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

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.Servlet;

import org.eclipse.equinox.jsp.jasper.JspServlet;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.http.context.ServletContextHelper;
import org.osgi.service.http.whiteboard.HttpWhiteboardConstants;

import de.dentrassi.osgi.web.DispatcherServlet;

public class Activator implements BundleActivator
{
    private static final String WEB_CONTEXT_NAME = "dispatcher";

    private static final String SERVLET_NAME = "dispatcher";

    private final List<ServiceRegistration<?>> regs = new LinkedList<> ();

    /*
     * (non-Javadoc)
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    @Override
    public void start ( final BundleContext context ) throws Exception
    {
        {
            final Dictionary<String, Object> properties = new Hashtable<> ();
            properties.put ( HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_NAME, WEB_CONTEXT_NAME );
            properties.put ( HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_PATH, "/" );

            final ServletContextImpl sctx = new ServletContextImpl ();
            addRegistration ( context.registerService ( ServletContextHelper.class, sctx, properties ) );
        }

        final String select = String.format ( "(%s=%s)", HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_NAME, WEB_CONTEXT_NAME );

        {
            final Dictionary<String, Object> properties = new Hashtable<> ();
            properties.put ( HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_NAME, SERVLET_NAME );
            properties.put ( HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_PATTERN, "/" );
            properties.put ( HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_SELECT, select );

            final Servlet servlet = new DispatcherServlet ();
            addRegistration ( context.registerService ( Servlet.class, servlet, properties ) );
        }

        {
            final Dictionary<String, Object> properties = new Hashtable<> ();
            properties.put ( HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_NAME, "jsp" );
            properties.put ( HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_PATTERN, "*.jsp" );
            properties.put ( HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_SELECT, select );

            final Servlet servlet = new JspServlet ( context.getBundle (), null, null );
            addRegistration ( context.registerService ( Servlet.class, servlet, properties ) );
        }
    }

    private void addRegistration ( final ServiceRegistration<?> reg )
    {
        this.regs.add ( reg );
    }

    /*
     * (non-Javadoc)
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop ( final BundleContext bundleContext ) throws Exception
    {
        for ( final ServiceRegistration<?> reg : this.regs )
        {
            reg.unregister ();
        }
        this.regs.clear ();
    }

}
