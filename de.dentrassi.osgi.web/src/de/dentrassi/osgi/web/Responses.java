package de.dentrassi.osgi.web;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public final class Responses
{
    private Responses ()
    {
    }

    public static final String LAST_MODIFIED = "Last-Modified";

    public static void methodNotAllowed ( final HttpServletRequest request, final HttpServletResponse response ) throws IOException
    {
        response.setStatus ( HttpServletResponse.SC_METHOD_NOT_ALLOWED );
    }

    public static void notFound ( final HttpServletRequest request, final HttpServletResponse response ) throws IOException
    {
        response.setStatus ( HttpServletResponse.SC_NOT_FOUND );
        response.getWriter ().println ( String.format ( "Resource '%s' could not be found", request.getServletPath () ) );
    }
}
