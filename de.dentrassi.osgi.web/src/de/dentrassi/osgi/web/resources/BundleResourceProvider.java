package de.dentrassi.osgi.web.resources;

import java.net.URL;

import org.osgi.framework.Bundle;

import de.dentrassi.osgi.web.RequestHandler;
import de.dentrassi.osgi.web.ResourceRequestHandler;

public class BundleResourceProvider implements ResourceHandlerProvider
{
    private final Bundle bundle;

    private final String resources;

    public BundleResourceProvider ( final Bundle bundle, final String resources )
    {
        this.bundle = bundle;
        this.resources = resources;
    }

    @Override
    public RequestHandler findHandler ( final String requestPath )
    {
        if ( !requestPath.startsWith ( this.resources ) )
        {
            return null;
        }

        final URL entry = this.bundle.getEntry ( requestPath );
        if ( entry != null )
        {
            return new ResourceRequestHandler ( entry, this.bundle.getLastModified () );
        }
        return null;
    }

}
