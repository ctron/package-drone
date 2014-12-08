package de.dentrassi.osgi.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dentrassi.osgi.web.controller.ControllerTracker;
import de.dentrassi.osgi.web.interceptor.InterceptorLocator;
import de.dentrassi.osgi.web.interceptor.InterceptorTracker;
import de.dentrassi.osgi.web.resources.ResourceTracker;

public class DispatcherServlet extends HttpServlet
{
    private final static Logger logger = LoggerFactory.getLogger ( DispatcherServlet.class );

    private static final long serialVersionUID = 1L;

    private RequestHandlerFactory resourceLocator;

    private RequestHandlerFactory controllerLocator;

    private InterceptorLocator interceptorLocator;

    @Override
    public void init () throws ServletException
    {
        super.init ();

        final BundleContext context = FrameworkUtil.getBundle ( DispatcherServlet.class ).getBundleContext ();

        this.resourceLocator = new ResourceTracker ( context );
        this.controllerLocator = new ControllerTracker ( context );
        this.interceptorLocator = new InterceptorTracker ( context );
    }

    @Override
    public void destroy ()
    {
        if ( this.resourceLocator != null )
        {
            this.resourceLocator.close ();
            this.resourceLocator = null;
        }
        if ( this.controllerLocator != null )
        {
            this.controllerLocator.close ();
            this.controllerLocator = null;
        }
        if ( this.interceptorLocator != null )
        {
            this.interceptorLocator.close ();
            this.interceptorLocator = null;
        }
        super.destroy ();
    }

    @Override
    protected void service ( final HttpServletRequest request, final HttpServletResponse response ) throws ServletException, IOException
    {
        logger.trace ( "service - {} ({})", request.getServletPath (), request );

        try
        {
            final Interceptor[] interceptors = this.interceptorLocator.getInterceptors ();

            runPreProcess ( interceptors, request, response );

            Exception ex = null;

            try
            {

                final RequestHandler requestHandler = mapRequest ( request, response );
                if ( requestHandler != null )
                {
                    runPostProcess ( interceptors, request, response, requestHandler );
                    requestHandler.process ( request, response );
                }
                else
                {
                    Responses.notFound ( request, response );
                }
            }
            catch ( final Exception e )
            {
                ex = e;
                throw new ServletException ( e );
            }
            finally
            {
                runAfterCompletion ( interceptors, request, response, ex );
            }
        }
        catch ( final Exception e )
        {
            throw new ServletException ( e );
        }
    }

    protected void runAfterCompletion ( final Interceptor[] interceptors, final HttpServletRequest request, final HttpServletResponse response, final Exception ex ) throws Exception
    {
        for ( final Interceptor i : interceptors )
        {
            i.afterCompletion ( request, response, ex );
        }
    }

    protected void runPostProcess ( final Interceptor[] interceptors, final HttpServletRequest request, final HttpServletResponse response, final RequestHandler result ) throws Exception
    {
        for ( final Interceptor i : interceptors )
        {
            i.postHandle ( request, response, result );
        }
    }

    protected boolean runPreProcess ( final Interceptor[] interceptors, final HttpServletRequest request, final HttpServletResponse response ) throws Exception
    {
        for ( final Interceptor i : interceptors )
        {
            if ( !i.preHandle ( request, response ) )
            {
                return false;
            }
        }
        return true;
    }

    protected RequestHandler mapRequest ( final HttpServletRequest request, final HttpServletResponse response )
    {
        RequestHandler handler;

        handler = this.resourceLocator.handleRequest ( request, response );

        if ( handler != null )
        {
            return handler;
        }

        handler = this.controllerLocator.handleRequest ( request, response );
        if ( handler != null )
        {
            return handler;
        }

        return null;
    }

}
