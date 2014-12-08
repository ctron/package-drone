package de.dentrassi.osgi.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.dentrassi.osgi.web.controller.ModelAndViewRequestHandler;

public abstract class ModelAndViewInterceptorAdapter extends InterceptorAdapter
{
    @Override
    public void postHandle ( final HttpServletRequest request, final HttpServletResponse response, final RequestHandler requestHandler ) throws Exception
    {
        if ( requestHandler instanceof ModelAndViewRequestHandler )
        {
            postHandle ( request, response, requestHandler, ( (ModelAndViewRequestHandler)requestHandler ).getModelAndView () );
        }
        super.postHandle ( request, response, requestHandler );
    }

    protected void postHandle ( final HttpServletRequest request, final HttpServletResponse response, final RequestHandler requestHandler, final ModelAndView modelAndView ) throws Exception
    {
    }
}
