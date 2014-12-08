package de.dentrassi.osgi.web.controller.routing;

import java.util.Map;

public interface PathMatcher
{
    public Map<String, String> matches ( String path );
}