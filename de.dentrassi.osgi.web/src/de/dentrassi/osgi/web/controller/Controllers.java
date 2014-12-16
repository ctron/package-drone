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
package de.dentrassi.osgi.web.controller;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.dentrassi.osgi.web.Controller;
import de.dentrassi.osgi.web.RequestMapping;

public final class Controllers
{
    public static List<RequestMapping> getRequestMappings ( final Method method )
    {
        final Class<?> dc = method.getDeclaringClass ();

        if ( !dc.isAnnotationPresent ( Controller.class ) )
        {
            return null;
        }

        return Arrays.asList ( method.getAnnotation ( RequestMapping.class ), dc.getAnnotation ( RequestMapping.class ) );
    }

    public static Set<String> getPaths ( final List<RequestMapping> mappings )
    {
        if ( mappings == null )
        {
            return null;
        }

        final Set<String> result = new HashSet<> ();

        for ( final RequestMapping rm : mappings )
        {
            if ( rm != null )
            {
                result.addAll ( Arrays.asList ( rm.value () ) );
            }
        }

        return result;
    }
}
