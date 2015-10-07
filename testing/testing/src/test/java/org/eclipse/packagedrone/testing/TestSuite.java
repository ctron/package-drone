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
package org.eclipse.packagedrone.testing;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.openqa.selenium.Platform;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;

@RunWith ( Suite.class )
@SuiteClasses ( { //
        UserTest.class, //
        MailTest.class, //
        UploadTest.class, //
        BasicTest.class, //
        DefaultTest.class, //
        DeployGroupTest.class, //
        MavenTest.class, //
        OsgiTest.class, //
        RpmTest.class, //
        DebTest.class, //
        MvnOsgiTest.class //
} )

public class TestSuite
{
    private static final String SAUCE_USER_NAME = System.getProperty ( "sauce.username" );

    private static final String SAUCE_ACCESS_KEY = System.getProperty ( "sauce.accessKey" );

    public static final int JETTY_PORT = Integer.getInteger ( "org.osgi.service.http.port", 8080 );

    private static RemoteWebDriver driver;

    public static RemoteWebDriver getDriver ()
    {
        return driver;
    }

    @BeforeClass
    public static void setupBrowser () throws MalformedURLException
    {
        if ( hasSauce () )
        {
            driver = createSauce ( Platform.WIN8_1, "chrome", null );
        }
        else
        {
            driver = new FirefoxDriver ();
        }
    }

    private static boolean hasSauce ()
    {
        return SAUCE_ACCESS_KEY != null && SAUCE_USER_NAME != null && !SAUCE_ACCESS_KEY.isEmpty () && !SAUCE_USER_NAME.isEmpty ();
    }

    protected static RemoteWebDriver createSauce ( final Platform os, final String browser, final String version ) throws MalformedURLException
    {
        final DesiredCapabilities capabilities = new DesiredCapabilities ();
        capabilities.setCapability ( CapabilityType.BROWSER_NAME, browser );
        if ( version != null )
        {
            capabilities.setCapability ( CapabilityType.VERSION, version );
        }
        capabilities.setCapability ( CapabilityType.PLATFORM, os );
        capabilities.setCapability ( "name", "Package Drone Main Test" );

        final RemoteWebDriver driver = new RemoteWebDriver ( new URL ( String.format ( "http://%s:%s@ondemand.saucelabs.com:80/wd/hub", SAUCE_USER_NAME, SAUCE_ACCESS_KEY ) ), capabilities );

        driver.setFileDetector ( new LocalFileDetector () );

        return driver;
    }

    @AfterClass
    public static void destroyBrowser ()
    {
        if ( driver != null )
        {
            driver.quit ();
        }
    }

    private static Process PROCESS;

    @BeforeClass
    public static void setup () throws IOException, InterruptedException
    {
        final String javaHome = System.getProperty ( "java.home" );

        final ProcessBuilder pb = new ProcessBuilder ( "target/instance/server" );

        pb.environment ().put ( "JAVA_HOME", javaHome );

        final Map<String, String> additional = new HashMap<> ();
        makeProcessSystemProperties ( pb, additional );

        pb.inheritIO ();

        System.out.println ( "Starting: " + pb );
        PROCESS = pb.start ();
        System.out.print ( "Started ... waiting for port... " );
        System.out.flush ();

        int i = 0;
        while ( i < 5 )
        {
            if ( isOpen ( JETTY_PORT ) )
            {
                break;
            }
            i++;
            Thread.sleep ( 1000 );
        }

        if ( i >= 5 )
        {
            PROCESS.destroyForcibly ();
            throw new IllegalStateException ( "Failed to wait for port" );
        }
        else
        {
            System.out.println ( "Port open!" );

            // sleep a little bit longer to allow OSGi to fire up all services
            Thread.sleep ( 1000 );
        }
    }

    private static void makeProcessSystemProperties ( final ProcessBuilder pb, final Map<String, String> additional )
    {
        final StringBuilder sb = new StringBuilder ();
        for ( final Map.Entry<Object, Object> entry : System.getProperties ().entrySet () )
        {
            if ( entry.getKey () == null || entry.getValue () == null )
            {
                continue;
            }

            final String key = entry.getKey ().toString ();
            final String value = entry.getValue ().toString ();

            if ( key.startsWith ( "org.osgi." ) || key.startsWith ( "drone." ) )
            {
                if ( sb.length () > 0 )
                {
                    sb.append ( ' ' );
                }
                sb.append ( "-D" ).append ( key ).append ( '=' ).append ( value );
            }
        }

        for ( final Map.Entry<String, String> entry : additional.entrySet () )
        {
            final String key = entry.getKey ();
            final String value = entry.getValue ();

            if ( sb.length () > 0 )
            {
                sb.append ( ' ' );
            }
            sb.append ( "-D" ).append ( key ).append ( '=' ).append ( value );
        }

        pb.environment ().put ( "JAVA_OPTS", sb.toString () );
    }

    private static boolean isOpen ( final int port )
    {
        ServerSocket server = null;
        try
        {
            server = new ServerSocket ( port );
            return true;
        }
        catch ( final IOException e )
        {
        }
        finally
        {
            if ( server != null )
            {
                try
                {
                    server.close ();
                }
                catch ( final IOException e )
                {
                }
            }
        }

        return false;
    }

    @AfterClass
    public static void dispose () throws InterruptedException
    {
        System.out.print ( "Terminating server..." );
        System.out.flush ();

        if ( !PROCESS.destroyForcibly ().waitFor ( 10, TimeUnit.SECONDS ) )
        {
            throw new IllegalStateException ( "Failed to terminate process" );
        }

        System.out.println ( "done!" );
    }

    public static String loadAdminToken ()
    {
        final Properties p = new Properties ();
        try ( InputStream in = new FileInputStream ( System.getProperty ( "user.home" ) + "/.drone-admin-token" ) )
        {
            p.load ( in );
        }
        catch ( final IOException e )
        {
            throw new RuntimeException ( e );
        }

        final String result = p.getProperty ( "password" );
        if ( result == null )
        {
            throw new IllegalStateException ( "Unable to find admin token" );
        }
        return result;
    }

}
