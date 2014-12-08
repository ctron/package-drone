package de.dentrassi.osgi.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class InterceptorAdapter implements Interceptor
{

    @Override
    public boolean preHandle ( final HttpServletRequest request, final HttpServletResponse response ) throws Exception
    {
        return true;
    }

    @Override
    public void postHandle ( final HttpServletRequest request, final HttpServletResponse response, final RequestHandler requestHandler ) throws Exception
    {
    }

    @Override
    public void afterCompletion ( final HttpServletRequest request, final HttpServletResponse response, final Exception ex ) throws Exception
    {
    }

}
