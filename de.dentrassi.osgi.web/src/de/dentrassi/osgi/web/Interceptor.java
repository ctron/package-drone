package de.dentrassi.osgi.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface Interceptor
{
    public boolean preHandle ( HttpServletRequest request, HttpServletResponse response ) throws Exception;

    public void postHandle ( HttpServletRequest request, HttpServletResponse response, RequestHandler requestHandler ) throws Exception;

    public void afterCompletion ( HttpServletRequest request, HttpServletResponse response, Exception ex ) throws Exception;
}
