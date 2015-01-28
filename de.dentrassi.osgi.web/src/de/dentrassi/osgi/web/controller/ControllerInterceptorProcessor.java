package de.dentrassi.osgi.web.controller;

import java.lang.reflect.Method;
import java.util.function.BiFunction;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.dentrassi.osgi.web.RequestHandler;

public interface ControllerInterceptorProcessor
{

    public default RequestHandler before ( final Object controller, final Method m, final HttpServletRequest request, final HttpServletResponse response, final BiFunction<HttpServletRequest, HttpServletResponse, RequestHandler> next ) throws Exception
    {
        return next.apply ( request, response );
    }

    public default RequestHandler after ( final Object controller, final Method m, final HttpServletRequest request, final HttpServletResponse response, final BiFunction<HttpServletRequest, HttpServletResponse, RequestHandler> next ) throws Exception
    {
        return next.apply ( request, response );
    }

}
