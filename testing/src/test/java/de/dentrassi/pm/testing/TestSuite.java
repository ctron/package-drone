package de.dentrassi.pm.testing;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith ( Suite.class )
@SuiteClasses ( { BasicTest.class, SetupTest.class } )
public class TestSuite
{

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
        System.out.println ( "Started" );

        Thread.sleep ( 5000 );
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
