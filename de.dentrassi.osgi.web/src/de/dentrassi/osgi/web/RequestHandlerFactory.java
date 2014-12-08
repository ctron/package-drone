package de.dentrassi.osgi.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface RequestHandlerFactory
{
    public RequestHandler handleRequest ( HttpServletRequest request, final HttpServletResponse response );

    public void close ();
}
