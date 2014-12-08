package de.dentrassi.osgi.web.controller.routing;

import java.util.Collections;
import java.util.Map;

public class PlainPathMatcher implements PathMatcher
{
    private final String path;

    public PlainPathMatcher ( final String path )
    {
        this.path = path;
    }

    @Override
    public Map<String, String> matches ( final String path )
    {
        if ( this.path.equals ( path ) )
        {
            return Collections.emptyMap ();
        }
        return null;
    }
}