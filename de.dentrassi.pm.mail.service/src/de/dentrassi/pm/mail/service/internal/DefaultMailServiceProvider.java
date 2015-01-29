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
package de.dentrassi.pm.mail.service.internal;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Properties;

import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;

public class DefaultMailServiceProvider implements ManagedService
{
    public final String PID = "de.dentrassi.pm.mail.service.default";

    private static final String PROPERTY_PREFIX = "property.";

    private DefaultMailService service;

    public DefaultMailServiceProvider ()
    {
    }

    public void start ()
    {
    }

    public void stop ()
    {
        stopService ();
    }

    @Override
    public void updated ( final Dictionary<String, ?> properties ) throws ConfigurationException
    {
        stopService ();

        if ( properties != null )
        {
            final String username = getString ( properties, "user" );
            final String password = getString ( properties, "password" );
            final String from = getString ( properties, "from" );

            final Properties props = new Properties ();

            final Enumeration<String> keys = properties.keys ();
            while ( keys.hasMoreElements () )
            {
                final String key = keys.nextElement ();
                if ( key.startsWith ( PROPERTY_PREFIX ) )
                {
                    props.put ( key.substring ( PROPERTY_PREFIX.length () ), properties.get ( key ) );
                }
            }

            this.service = new DefaultMailService ( username, password, props, from );
            this.service.start ();
        }
    }

    private String getString ( final Dictionary<String, ?> properties, final String key )
    {
        final Object value = properties.get ( key );
        if ( value instanceof String )
        {
            return (String)value;
        }
        return null;
    }

    protected void stopService ()
    {
        if ( this.service != null )
        {
            this.service.stop ();
            this.service = null;
        }
    }
}
