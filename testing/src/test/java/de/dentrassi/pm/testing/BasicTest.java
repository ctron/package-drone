package de.dentrassi.pm.testing;

import java.io.InputStream;

import org.junit.Test;

public class BasicTest extends AbstractServerTest
{
    @Test
    public void test1 () throws Exception
    {
        System.out.println ( "Test 1" );
        try ( InputStream is = getUrl ().openStream () )
        {
        }
    }
}
