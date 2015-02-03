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

public class BooleanToStringConverter implements Converter
{
    public static final BooleanToStringConverter INSTANCE = new BooleanToStringConverter ();

    @Override
    public boolean canConvert ( final Class<?> from, final Class<?> to )
    {
        if ( from.equals ( Boolean.class ) && to.equals ( String.class ) )
        {
            return true;
        }
        return false;
    }

    @Override
    public String convertTo ( final Object value )
    {
        if ( value == null )
        {
            return null;
        }
        return value.toString ();
    }
}
