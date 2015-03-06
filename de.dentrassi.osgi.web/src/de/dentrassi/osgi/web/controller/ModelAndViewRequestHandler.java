/*******************************************************************************
 * Copyright (c) 2014, 2015 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package de.dentrassi.osgi.web.controller;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dentrassi.osgi.web.ModelAndView;
import de.dentrassi.osgi.web.RequestHandler;
import de.dentrassi.osgi.web.ViewResolver;

public class ModelAndViewRequestHandler implements RequestHandler
{
    private final static Logger logger = LoggerFactory.getLogger ( ModelAndViewRequestHandler.class );

    private final ModelAndView modelAndView;

    private final Class<?> controllerClazz;

    private final Method method;

    public ModelAndViewRequestHandler ( final ModelAndView modelAndView, final Class<?> controllerClazz, final Method method )
    {
        this.modelAndView = modelAndView;
        this.controllerClazz = controllerClazz;
        this.method = method;
    }

    public ModelAndView getModelAndView ()
    {
        return this.modelAndView;
    }

    @Override
    public void process ( final HttpServletRequest request, final HttpServletResponse response ) throws IOException, ServletException
    {
        final String redir = this.modelAndView.getRedirect ();
        if ( redir != null )
        {
            logger.debug ( "Processed redirect: {}", redir );
            response.sendRedirect ( redir );
            return;
        }

        ViewResolver viewResolver = null;
        Class<?> resourceClazz = this.controllerClazz;

        if ( this.modelAndView.getAlternateViewResolver () != null )
        {
            viewResolver = this.modelAndView.getAlternateViewResolver ().getAnnotation ( ViewResolver.class );
            resourceClazz = this.modelAndView.getAlternateViewResolver ();
        }

        if ( viewResolver == null && this.method != null )
        {
            viewResolver = this.method.getAnnotation ( ViewResolver.class );
        }

        if ( viewResolver == null && this.controllerClazz != null )
        {
            viewResolver = this.controllerClazz.getAnnotation ( ViewResolver.class );
        }

        if ( viewResolver == null )
        {
            throw new IllegalStateException ( String.format ( "View resolver for %s not declared. Missing @%s annotation?", this.controllerClazz.getName (), ViewResolver.class.getSimpleName () ) );
        }

        final Bundle bundle = FrameworkUtil.getBundle ( resourceClazz );

        final String resolvedView = String.format ( viewResolver.value (), this.modelAndView.getViewName () );
        final String path = String.format ( "/bundle/%s/%s", bundle.getBundleId (), resolvedView );

        logger.debug ( "Render: {}", path );

        setModelAsRequestAttributes ( request, this.modelAndView.getModel () );

        final RequestDispatcher rd = request.getRequestDispatcher ( path );
        if ( response.isCommitted () )
        {
            logger.trace ( "Including" );
            rd.include ( request, response );
        }
        else
        {
            logger.trace ( "Forwarding" );
            rd.forward ( request, response );
        }
    }

    private void setModelAsRequestAttributes ( final HttpServletRequest request, final Map<String, Object> model )
    {
        for ( final Map.Entry<String, Object> entry : model.entrySet () )
        {
            request.setAttribute ( entry.getKey (), entry.getValue () );
        }
    }

    @Override
    public String toString ()
    {
        return String.format ( "[RequestHandler/ModelAndView - %s]", this.modelAndView );
    }
}
