package de.dentrassi.pm.testing;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class AbstractServerTest
{

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
