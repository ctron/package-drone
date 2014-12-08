package de.dentrassi.osgi.web.controller.converter;

import java.util.Collection;
import java.util.LinkedList;

public class ConverterManager
{
    private final Collection<Converter<?>> converters = new LinkedList<> ();

    public static ConverterManager create ()
    {
        final ConverterManager result = new ConverterManager ();

        result.addConverter ( IntegerConverter.INSTANCE );

        return result;
    }

    public ConverterManager ()
    {
    }

    public void addConverter ( final Converter<?> converter )
    {
        this.converters.add ( converter );
    }

    public <T> T convertTo ( final String value, final Class<T> clazz )
    {
        if ( clazz.isAssignableFrom ( String.class ) )
        {
            return clazz.cast ( value );
        }

        for ( final Converter<?> cvt : this.converters )
        {
            if ( clazz.equals ( cvt.getType () ) )
            {
                final Object o = cvt.convertTo ( value );
                if ( o == null )
                {
                    return null;
                }

                if ( clazz.isAssignableFrom ( o.getClass () ) )
                {
                    return clazz.cast ( o );
                }
                else
                {
                    throw new ConversionException ( String.format ( "Invalid result type (expected: %s, actual: %s)", clazz.getName (), o.getClass ().getName () ) );
                }
            }
        }
        throw new ConversionException ( String.format ( "Unable to convert to %s", clazz.getName () ) );
    }
}
