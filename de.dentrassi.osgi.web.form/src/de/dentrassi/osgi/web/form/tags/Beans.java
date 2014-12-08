package de.dentrassi.osgi.web.form.tags;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class Beans
{
    private Beans ()
    {
    }

    public static String makeGetter ( final String propertyName )
    {
        if ( propertyName == null || propertyName.isEmpty () )
        {
            throw new IllegalArgumentException ( String.format ( "Property name must not be empty or null" ) );
        }

        final StringBuilder sb = new StringBuilder ( propertyName.length () + 3 );

        sb.append ( "get" );
        sb.append ( propertyName.substring ( 0, 1 ).toUpperCase () );
        sb.append ( propertyName.substring ( 1 ) );

        return sb.toString ();
    }

    public static Object getFromBean ( final Object o, final String propertyName ) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException
    {
        final Method m = o.getClass ().getMethod ( makeGetter ( propertyName ) );
        return m.invoke ( o );
    }
}
