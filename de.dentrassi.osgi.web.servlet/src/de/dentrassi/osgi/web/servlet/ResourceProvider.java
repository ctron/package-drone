package de.dentrassi.osgi.web.servlet;

import java.net.URL;
import java.util.Set;

public interface ResourceProvider
{
    public URL getResource ( String name );

    public Set<String> getPaths ( String path );

    public default void dispose ()
    {
    }
}
