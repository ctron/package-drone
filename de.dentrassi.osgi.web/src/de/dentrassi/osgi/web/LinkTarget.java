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
package de.dentrassi.osgi.web;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.PageContext;

import org.eclipse.scada.utils.str.StringReplacer;
import org.eclipse.scada.utils.str.StringReplacer.ReplaceSource;

import de.dentrassi.osgi.web.controller.Controllers;
import de.dentrassi.osgi.web.controller.routing.RequestMappingInformation;

public class LinkTarget
{
    private static final Pattern PATTERN = Pattern.compile ( "\\{(.*?)\\}" );

    private final String url;

    public LinkTarget ( final String url )
    {
        this.url = url;
    }

    public String render ( final ServletRequest request )
    {
        return expand ( new ReplaceSource () {

            @Override
            public String replace ( final String context, final String key )
            {
                final Object v = request.getAttribute ( key );
                if ( v == null )
                {
                    return context;
                }
                else
                {
                    return v.toString ();
                }
            }
        } ).getUrl ();
    }

    public String render ( final PageContext pageContext )
    {
        return render ( pageContext.getRequest () );
    }

    public String renderFull ( final PageContext pageContext )
    {
        final StringBuilder sb = new StringBuilder ( pageContext.getServletContext ().getContextPath () );

        if ( sb.length () > 0 && !sb.substring ( sb.length () - 1 ).equals ( "/" ) )
        {
            sb.append ( '/' );
        }

        sb.append ( render ( pageContext.getRequest () ) );

        return sb.toString ();
    }

    public String render ( final Map<String, ?> model )
    {
        return expand ( StringReplacer.newExtendedSource ( model ) ).getUrl ();
    }

    public LinkTarget expand ( final Map<String, ?> model )
    {
        return expand ( StringReplacer.newExtendedSource ( model ) );
    }

    public LinkTarget expand ( final ReplaceSource source )
    {
        if ( this.url == null || source == null )
        {
            return this;
        }

        return new LinkTarget ( StringReplacer.replace ( this.url, source, PATTERN, false ) );
    }

    public String getUrl ()
    {
        return this.url;
    }

    private static Set<String> getRawPaths ( final Method method )
    {
        final RequestMappingInformation rmi = Controllers.fromMethod ( method );
        if ( rmi == null )
        {
            return null;
        }

        return rmi.getRawPaths ();
    }

    public static LinkTarget createFromController ( final Class<?> controllerClazz, final String methodName )
    {
        for ( final Method m : controllerClazz.getMethods () )
        {
            if ( !m.getName ().equals ( methodName ) )
            {
                continue;
            }

            final Set<String> paths = getRawPaths ( m );
            if ( paths.isEmpty () )
            {
                continue;
            }

            return new LinkTarget ( paths.iterator ().next () );
        }

        throw new IllegalArgumentException ( String.format ( "Controller class '%s' has no request method '%s'", controllerClazz.getName (), methodName ) );
    }

    public static LinkTarget createFromController ( final Method method )
    {
        final Set<String> paths = getRawPaths ( method );
        if ( paths.isEmpty () )
        {
            throw new IllegalStateException ( String.format ( "Method '%s' has no @RequestMapping information assigned", method ) );
        }

        return new LinkTarget ( paths.iterator ().next () );
    }
}
