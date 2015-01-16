package de.dentrassi.pm.testing;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Test;

public class BasicTest extends AbstractServerTest
{
    public URL getUrl () throws MalformedURLException
    {
        return new URL ( "http://localhost:8080" );
    }

    @Test
    public void test1 () throws Exception
    {
        System.out.println ( "Test 1" );
        try ( InputStream is = getUrl ().openStream () )
        {
        }
    }
}
