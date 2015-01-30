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

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.TimeUnit;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

@RunWith ( Suite.class )
@SuiteClasses ( { BasicTest.class, SetupTest.class, DefaultTest.class, MailTest.class, UploadTest.class } )
public class TestSuite
{

    private static RemoteWebDriver driver;

    public static RemoteWebDriver getDriver ()
    {
        return driver;
    }

    @BeforeClass
    public static void setupBrowser ()
    {
        driver = new FirefoxDriver ();
    }

    @AfterClass
    public static void destroyBrowser ()
    {
        driver.close ();
    }

    private static Process PROCESS;

    @BeforeClass
    public static void setup () throws IOException, InterruptedException
    {
        final String javaHome = System.getProperty ( "java.home" );

        final ProcessBuilder pb = new ProcessBuilder ( "target/instance/server" );

        pb.environment ().put ( "JAVA_HOME", javaHome );

        pb.inheritIO ();

        System.out.println ( "Starting: " + pb );
        PROCESS = pb.start ();
        System.out.print ( "Started ... waiting for port... " );
        System.out.flush ();

        int i = 0;
        while ( i < 5 )
        {
            if ( isOpen ( 8080 ) )
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
        }
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

}
