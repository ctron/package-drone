package de.dentrassi.osgi.web.controller;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
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
        final RequestMapping ca = m.getDeclaringClass ().getAnnotation ( RequestMapping.class );
        final RequestMapping ma = m.getAnnotation ( RequestMapping.class );

        final Set<String> paths = new HashSet<> ();
        final Set<String> methods = new HashSet<> ();

        if ( ma != null )
        {
            paths.addAll ( Arrays.asList ( ma.value () ) );
            addMethods ( methods, ma.method () );
        }
        if ( ca != null )
        {
            paths.addAll ( Arrays.asList ( ca.value () ) );
            addMethods ( methods, ma.method () );
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
