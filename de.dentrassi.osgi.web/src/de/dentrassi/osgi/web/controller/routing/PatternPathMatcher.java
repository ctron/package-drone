package de.dentrassi.osgi.web.controller.routing;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternPathMatcher implements PathMatcher
{
    private final Pattern pattern;

    private final String[] names;

    public PatternPathMatcher ( final Pattern pattern, final String[] names )
    {
        this.pattern = pattern;
        this.names = names;
    }

    @Override
    public Map<String, String> matches ( final String path )
    {
        final Matcher m = this.pattern.matcher ( path );
        if ( !m.matches () )
        {
            return null;
        }

        final Map<String, String> result = new HashMap<> ( this.names.length );

        for ( int i = 0; i < this.names.length; i++ )
        {
            result.put ( this.names[i], m.group ( i + 1 ) );
        }

        return result;
    }

}
