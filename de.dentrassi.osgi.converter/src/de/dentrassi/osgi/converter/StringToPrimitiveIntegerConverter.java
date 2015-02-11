/*******************************************************************************
 * Copyright (c) 2015 Jens Reimann.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jens Reimann - initial API and implementation
 *******************************************************************************/
package de.dentrassi.osgi.converter;

public class StringToPrimitiveIntegerConverter implements Converter
{
    public static final StringToPrimitiveIntegerConverter INSTANCE = new StringToPrimitiveIntegerConverter ();

    @Override
    public boolean canConvert ( final Class<?> from, final Class<?> to )
    {
        if ( from.equals ( String.class ) && to.equals ( int.class ) )
        {
            return true;
        }
        return false;
    }

    @Override
    public Object convertTo ( final Object value, final Class<?> clazz )
    {
        if ( value == null )
        {
            return null;
        }

        final String str = value.toString ();

        return Integer.parseInt ( str );
    }
}