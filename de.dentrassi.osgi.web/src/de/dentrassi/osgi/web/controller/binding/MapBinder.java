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
package de.dentrassi.osgi.web.controller.binding;

import java.util.HashMap;
import java.util.Map;

import de.dentrassi.osgi.converter.ConverterManager;

public class MapBinder implements Binder
{
    private final Map<String, Object> objects;

    public MapBinder ( final Map<String, Object> objects )
    {
        this.objects = objects;
    }

    public MapBinder ()
    {
        this.objects = new HashMap<> ();
    }

    protected Map<String, Object> getObjects ()
    {
        return this.objects;
    }

    @Override
    public Binding performBind ( final BindTarget target, final ConverterManager converter, final BindingManager bindingManager )
    {
        final Class<?> type = target.getType ();

        final String q = target.getQualifier ();

        if ( q != null )
        {
            final Object value = this.objects.get ( q );
            if ( value != null )
            {
                return Binding.simpleBinding ( converter.convertTo ( value, type ) );
            }
        }
        else
        {
            for ( final Map.Entry<String, Object> entry : this.objects.entrySet () )
            {
                final Object o = entry.getValue ();
                if ( o == null )
                {
                    continue;
                }

                if ( type.isAssignableFrom ( o.getClass () ) )
                {
                    return Binding.simpleBinding ( o );
                }
            }
        }

        return null;
    }
}
