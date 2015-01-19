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
package de.dentrassi.pm.testing;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.BeforeClass;
import org.openqa.selenium.remote.RemoteWebDriver;

public class AbstractServerTest
{
    protected static RemoteWebDriver driver;

    @BeforeClass
    public static void setup ()
    {
        driver = TestSuite.getDriver ();
    }

    protected String getTestUser ()
    {
        return System.getProperty ( "mysql.test.user" );
    }

    protected String getTestPassword ()
    {
        return System.getProperty ( "mysql.test.password" );
    }

    protected String getTestJdbcUrl ()
    {
        return String.format ( "jdbc:mysql://localhost/%s", System.getProperty ( "mysql.test.database" ) );
    }

    protected URL getUrl () throws MalformedURLException
    {
        return new URL ( getBase () );
    }

    protected String getBase ()
    {
        return "http://localhost:8080";
    }

    protected String resolve ( final String suffix )
    {
        try
        {
            return new URI ( getBase () ).resolve ( suffix ).toString ();
        }
        catch ( final URISyntaxException e )
        {
            throw new RuntimeException ( e );
        }
    }
}
