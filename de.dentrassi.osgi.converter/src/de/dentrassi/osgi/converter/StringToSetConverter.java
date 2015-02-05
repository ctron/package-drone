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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class StringToSetConverter implements Converter
{
    public static final StringToSetConverter INSTANCE = new StringToSetConverter ();

    @Override
    public boolean canConvert ( final Class<?> from, final Class<?> to )
    {
        if ( ( from.equals ( String[].class ) || from.equals ( String.class ) ) && to.equals ( Set.class ) )
        {
            return true;
        }
        return false;
    }

    @Override
    public Set<?> convertTo ( final Object value )
    {
        if ( value == null )
        {
            return null;
        }

        if ( value instanceof String[] )
        {
            return convertFromArray ( (String[])value );
        }
        else
        {
            return convertFromValue ( value.toString () );
        }
    }

    private Set<?> convertFromValue ( final String string )
    {
        return Collections.singleton ( string );
    }

    private Set<?> convertFromArray ( final String[] value )
    {
        return new HashSet<> ( Arrays.asList ( value ) );
    }
}
