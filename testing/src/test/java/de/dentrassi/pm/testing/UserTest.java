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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.support.ui.Select;

public class UserTest extends AbstractServerTest
{

    private static final String TEST_USER_REAL_NAME = "Drone Tester";

    private static final String TEST_USER_EMAIL = "dronetester@dentrassi.de";

    private static final String TEST_USER_PASSWORD = "123456";

    @Test
    public void testConfig ()
    {
        driver.get ( resolve ( "/user" ) );

        // check if we are on the right page

        Assert.assertEquals ( resolve ( "/user" ), driver.getCurrentUrl () );

        // add

        driver.get ( resolve ( "/user/add" ) );

        Assert.assertEquals ( resolve ( "/user/add" ), driver.getCurrentUrl () );

        // enter

        driver.findElementById ( "email" ).sendKeys ( TEST_USER_EMAIL );
        driver.findElementById ( "name" ).sendKeys ( TEST_USER_REAL_NAME );
        driver.findElementById ( "command" ).submit ();

        Assert.assertTrue ( driver.getCurrentUrl ().endsWith ( "/view" ) );

        final String userId = driver.findElementById ( "userId" ).getText ();

        driver.findElementByClassName ( "btn-primary" ).click ();

        Assert.assertTrue ( driver.getCurrentUrl ().endsWith ( "/" + userId + "/edit" ) );

        final Select select = new Select ( driver.findElementById ( "roles" ) );
        select.selectByValue ( "ADMIN" );
        select.selectByValue ( "MANAGER" );
        driver.findElementById ( "command" ).submit ();

        Assert.assertTrue ( driver.getCurrentUrl ().endsWith ( "/" + userId + "/view" ) );

        driver.get ( resolve ( "/logout" ) );

        setPassword ( userId, TEST_USER_PASSWORD );

        driver.get ( resolve ( "/login" ) );

        driver.findElementById ( "email" ).sendKeys ( TEST_USER_EMAIL );
        driver.findElementById ( "password" ).sendKeys ( TEST_USER_PASSWORD );
        driver.findElementById ( "command" ).submit ();
    }

    private void setPassword ( final String userId, final String password )
    {
        final String salt = Tokens.createToken ( 32 );
        final String hash = Tokens.hashIt ( salt, password );

        try
        {
            try ( final Connection con = DriverManager.getConnection ( getTestJdbcUrl (), getTestUser (), getTestUser () ) )
            {
                try ( PreparedStatement stmt = con.prepareStatement ( "UPDATE USERS SET PASSWORD_HASH=?, PASSWORD_SALT=? WHERE ID=?" ) )
                {
                    stmt.setString ( 1, hash );
                    stmt.setString ( 2, salt );
                    stmt.setString ( 3, userId );
                    final int count = stmt.executeUpdate ();
                    Assert.assertEquals ( "Number of changed users", 1, count );
                }
            }
        }
        catch ( final Exception e )
        {
            throw new RuntimeException ( e );
        }
    }
}
