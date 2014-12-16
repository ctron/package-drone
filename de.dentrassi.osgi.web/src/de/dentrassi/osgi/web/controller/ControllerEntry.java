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
package de.dentrassi.osgi.web.controller;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.dentrassi.osgi.web.ModelAndView;
import de.dentrassi.osgi.web.RequestHandler;
import de.dentrassi.osgi.web.RequestMapping;
import de.dentrassi.osgi.web.RequestMethod;
import de.dentrassi.osgi.web.ViewResolver;
import de.dentrassi.osgi.web.controller.binding.BindingManager;
import de.dentrassi.osgi.web.controller.binding.PathVariableBinder;
import de.dentrassi.osgi.web.controller.binding.RequestParameterBinder;
import de.dentrassi.osgi.web.controller.form.FormDataBinder;
import de.dentrassi.osgi.web.controller.routing.RequestMappingInformation;
import de.dentrassi.osgi.web.controller.routing.RequestMappingInformation.Match;

public class ControllerEntry
{
    private final Object controller;

    private final class Call
    {
        private final RequestMappingInformation rmi;

        private final Method m;

        public Call ( final RequestMappingInformation rmi, final Method m )
        {
            this.rmi = rmi;
            this.m = m;
        }

        public Match matches ( final HttpServletRequest request )
        {
            return this.rmi.matches ( request );
        }

        public RequestHandler call ( final Match match, final HttpServletRequest request, final HttpServletResponse response )
        {
            try
            {

                final Map<String, Object> data = new HashMap<String, Object> ();

                final BindingManager manager = BindingManager.create ( data );
                data.put ( "request", request );
                data.put ( "response", response );

                manager.addBinder ( new RequestParameterBinder ( request ) );
                manager.addBinder ( new PathVariableBinder ( match ) );
                manager.addBinder ( new FormDataBinder ( request ) );

                final de.dentrassi.osgi.web.controller.binding.BindingManager.Call call = manager.bind ( this.m, ControllerEntry.this.controller );
                final Object result = call.invoke ();

                if ( result instanceof ModelAndView )
                {
                    return new ModelAndViewRequestHandler ( (ModelAndView)result, ControllerEntry.this.controller.getClass () );
                }
                else if ( result instanceof String )
                {
                    return new ModelAndViewRequestHandler ( new ModelAndView ( (String)result ), ControllerEntry.this.controller.getClass () );
                }
                else if ( result == null )
                {
                    return new NoOpRequestHandler ();
                }
                else
                {
                    throw new IllegalStateException ( String.format ( "Response type %s is unsupported", result.getClass () ) );
                }
            }
            catch ( final Exception e )
            {
                throw new RuntimeException ( e );
            }
        }
    }

    private final Set<Call> calls = new HashSet<> ();

    private final ViewResolver viewResolver;

    public ControllerEntry ( final Object controller )
    {
        this.controller = controller;

        final Class<? extends Object> clazz = controller.getClass ();

        this.viewResolver = clazz.getAnnotation ( ViewResolver.class );

        for ( final Method m : clazz.getMethods () )
        {
            final RequestMappingInformation rmi = parse ( m );
            if ( rmi != null )
            {
                this.calls.add ( new Call ( rmi, m ) );
            }
        }
    }

    public Object getController ()
    {
        return this.controller;
    }

    public ViewResolver getViewResolver ()
    {
        return this.viewResolver;
    }

    protected static RequestMappingInformation parse ( final Method m )
    {
        final List<RequestMapping> mappings = Controllers.getRequestMappings ( m );

        if ( mappings == null )
        {
            return null;
        }

        final Set<String> paths = Controllers.getPaths ( mappings );

        final Set<String> methods = new HashSet<> ();

        for ( final RequestMapping rm : mappings )
        {
            if ( rm != null )
            {
                addMethods ( methods, rm.method () );
            }
        }

        if ( paths.isEmpty () )
        {
            return null;
        }
        else
        {
            return new RequestMappingInformation ( paths, methods );
        }
    }

    private static void addMethods ( final Set<String> result, final RequestMethod[] methods )
    {
        if ( methods == null )
        {
            return;
        }

        for ( final RequestMethod m : methods )
        {
            result.add ( m.name () );
        }
    }

    public RequestHandler findHandler ( final HttpServletRequest request, final HttpServletResponse response )
    {
        for ( final Call call : this.calls )
        {
            final Match match = call.matches ( request );
            if ( match == null )
            {
                continue;
            }

            final RequestHandler handler = call.call ( match, request, response );
            if ( handler != null )
            {
                return handler;
            }
        }
        return null;
    }

    @Override
    public String toString ()
    {
        return String.format ( "[Controller: %s]", this.controller );
    }

}
