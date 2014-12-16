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
package de.dentrassi.osgi.web.controller.converter;

public class IntegerConverter implements Converter<Integer>
{
    public static final IntegerConverter INSTANCE = new IntegerConverter ();

    @Override
    public Class<Integer> getType ()
    {
        return Integer.class;
    }

    @Override
    public Integer convertTo ( final String value )
    {
        try
        {
            return Integer.parseInt ( value );
        }
        catch ( final NumberFormatException e )
        {
            throw new ConversionException ( e );
        }
    }
}
