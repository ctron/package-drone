package de.dentrassi.osgi.web.controller;

import java.io.IOException;
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

    public ModelAndViewRequestHandler ( final ModelAndView modelAndView, final Class<?> controllerClazz )
    {
        this.modelAndView = modelAndView;
        this.controllerClazz = controllerClazz;
    }

    public ModelAndView getModelAndView ()
    {
        return this.modelAndView;
    }

    @Override
    public void process ( final HttpServletRequest request, final HttpServletResponse response ) throws IOException, ServletException
    {
        final ViewResolver viewResolver = this.controllerClazz.getAnnotation ( ViewResolver.class );

        if ( viewResolver == null )
        {
            throw new IllegalStateException ( String.format ( "View resolver for %s not declared. Missing @%s annotation?", this.controllerClazz.getName (), ViewResolver.class.getSimpleName () ) );
        }

        if ( this.modelAndView.isRedirect () )
        {
            final String redir = this.modelAndView.getRedirect ();
            logger.debug ( "Processing redirect: {}", redir );
            response.sendRedirect ( redir );
        }
        else
        {
            final Bundle bundle = FrameworkUtil.getBundle ( this.controllerClazz );

            final String path = "/bundle/" + bundle.getBundleId () + "/" + String.format ( viewResolver.value (), this.modelAndView.getViewName () );

            logger.debug ( "Render: {}", path );

            setModelAsRequestAttributes ( request, this.modelAndView.getModel () );

            final RequestDispatcher rd = request.getRequestDispatcher ( path );

            if ( response.isCommitted () )
            {
                rd.forward ( request, response );
            }
            else
            {
                rd.include ( request, response );
            }
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
