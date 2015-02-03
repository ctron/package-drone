/*******************************************************************************
 * Copyright (c) 2014 Jens Reimann.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jens Reimann - initial API and implementation
 *******************************************************************************/
package de.dentrassi.osgi.converter;

import java.util.Collection;
import java.util.LinkedList;

public class ConverterManager
{
    private final Collection<Converter> converters = new LinkedList<> ();

    public static ConverterManager create ()
    {
        final ConverterManager result = new ConverterManager ();

        result.addConverter ( StringToIntegerConverter.INSTANCE );
        result.addConverter ( StringToBooleanConverter.INSTANCE );
        result.addConverter ( StringToPrimitiveBooleanConverter.INSTANCE );
        result.addConverter ( BooleanToStringConverter.INSTANCE );

        return result;
    }

    public ConverterManager ()
    {
    }

    public void addConverter ( final Converter converter )
    {
        this.converters.add ( converter );
    }

    @SuppressWarnings ( "unchecked" )
    public <T> T convertTo ( final Object value, final Class<T> clazz )
    {
        if ( value == null )
        {
            return null;
        }

        if ( clazz.isAssignableFrom ( value.getClass () ) )
        {
            return clazz.cast ( value );
        }

        final Class<?> from = value.getClass ();

        for ( final Converter cvt : this.converters )
        {
            if ( cvt.canConvert ( from, clazz ) )
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
                else if ( clazz.isPrimitive () )
                {
                    return (T)o;
                }
                else
                {
                    throw new ConversionException ( String.format ( "Invalid result type (expected: %s, actual: %s)", clazz.getName (), o.getClass ().getName () ) );
                }
            }
        }
        throw new ConversionException ( String.format ( "Unable to convert %s to %s", value.getClass (), clazz.getName () ) );
    }
}
