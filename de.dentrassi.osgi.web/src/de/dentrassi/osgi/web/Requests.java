package de.dentrassi.osgi.web;

import javax.servlet.http.HttpServletRequest;

public final class Requests
{
    public static final String IF_MODIFIED_SINCE = "If-Modified-Since";

    private Requests ()
    {
    }

    public static boolean isNotModified ( final HttpServletRequest request, final long lastModified )
    {
        if ( lastModified < 0 )
        {
            return false;
        }

        final long modifiedSince = getIfModifiedSince ( request );
        if ( modifiedSince <= 0 )
        {
            return false;
        }

        return lastModified >= modifiedSince;
    }

    private static long getIfModifiedSince ( final HttpServletRequest request )
    {
        try
        {
            return request.getDateHeader ( IF_MODIFIED_SINCE );
        }
        catch ( final Exception e )
        {
            return -1;
        }
    }
}
