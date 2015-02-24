/*******************************************************************************
 * Copyright (c) 2014 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package de.dentrassi.pm.database;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.jdbc.DataSourceFactory;

public class JdbcHelper
{

    public static List<JdbcDriverInformation> getJdbcDrivers ()
    {
        final List<JdbcDriverInformation> result = new ArrayList<> ();

        final BundleContext context = FrameworkUtil.getBundle ( JdbcHelper.class ).getBundleContext ();

        try
        {
            final Collection<ServiceReference<DataSourceFactory>> refs = context.getServiceReferences ( DataSourceFactory.class, null );
            for ( final ServiceReference<DataSourceFactory> ref : refs )
            {
                final String className = getString ( ref.getProperty ( "osgi.jdbc.driver.class" ) );
                String name = getString ( ref.getProperty ( "osgi.jdbc.driver.name" ) );
                final String version = getString ( ref.getProperty ( "osgi.jdbc.driver.version" ) );

                if ( className == null )
                {
                    continue;
                }

                if ( name == null )
                {
                    name = className;
                }

                result.add ( new JdbcDriverInformation ( className, name, version ) );
            }
        }
        catch ( final InvalidSyntaxException e )
        {
        }

        Collections.sort ( result, new Comparator<JdbcDriverInformation> () {

            @Override
            public int compare ( final JdbcDriverInformation o1, final JdbcDriverInformation o2 )
            {
                return o1.getName ().compareTo ( o2.getName () );
            }
        } );

        return result;
    }

    private static String getString ( final Object value )
    {
        if ( value instanceof String )
        {
            return (String)value;
        }
        return null;
    }
}
