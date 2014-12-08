package de.dentrassi.osgi.web.resources;

import de.dentrassi.osgi.web.RequestHandler;

public interface ResourceHandlerProvider
{
    public RequestHandler findHandler ( String requestPath );
}
