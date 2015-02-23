/*******************************************************************************
 * Copyright (c) 2015 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package de.dentrassi.pm.database;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Properties;

import org.hibernate.validator.constraints.NotEmpty;

import de.dentrassi.osgi.utils.Dictionaries;

public class DatabaseConnectionData
{
    @NotEmpty
    private String jdbcDriver;

    @NotEmpty
    private String url;

    private String user;

    private String password;

    private String additionalProperties;

    public String getJdbcDriver ()
    {
        return this.jdbcDriver;
    }

    public void setJdbcDriver ( final String jdbcDriver )
    {
        this.jdbcDriver = jdbcDriver;
    }

    public String getUrl ()
    {
        return this.url;
    }

    public void setUrl ( final String url )
    {
        this.url = url;
    }

    public String getUser ()
    {
        return this.user;
    }

    public void setUser ( final String user )
    {
        this.user = user;
    }

    public String getPassword ()
    {
        return this.password;
    }

    public void setPassword ( final String password )
    {
        this.password = password;
    }

    public String getAdditionalProperties ()
    {
        return this.additionalProperties;
    }

    public void setAdditionalProperties ( final String additionalProperties )
    {
        this.additionalProperties = additionalProperties;
    }

    public static DatabaseConnectionData fromProperties ( Dictionary<String, Object> props ) throws IOException
    {
        props = Dictionaries.copy ( props );

        final DatabaseConnectionData result = new DatabaseConnectionData ();
        result.setJdbcDriver ( getString ( props, "javax.persistence.jdbc.driver" ) );
        result.setUrl ( getString ( props, "javax.persistence.jdbc.url" ) );
        result.setUser ( getString ( props, "javax.persistence.jdbc.user" ) );
        result.setPassword ( getString ( props, "javax.persistence.jdbc.password" ) );

        final Properties p = new Properties ();
        final Enumeration<String> i = props.keys ();
        while ( i.hasMoreElements () )
        {
            final String key = i.nextElement ();
            if ( !key.startsWith ( "javax.persistence.jdbc." ) )
            {
                continue;
            }
            p.put ( key.substring ( "javax.persistence.jdbc.".length () ), props.get ( key ) );
        }

        final StringWriter sw = new StringWriter ();
        p.store ( sw, null );
        sw.close ();
        result.setAdditionalProperties ( sw.getBuffer ().toString ().replaceAll ( "^#.*", "" ) );
        return result;
    }

    private static String getString ( final Dictionary<String, Object> props, final String string )
    {
        final Object o = props.remove ( string );
        if ( o instanceof String )
        {
            return (String)o;
        }

        return null;
    }

}
