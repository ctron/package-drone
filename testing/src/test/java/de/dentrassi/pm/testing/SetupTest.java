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

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.common.base.Predicate;

public class SetupTest extends AbstractServerTest
{

    @Test
    public void testSetup () throws Exception
    {
        driver.get ( getBase () );

        // should redirect us

        Assert.assertEquals ( resolve ( "/setup" ), driver.getCurrentUrl () );

        // Sign in first

        final String adminToken = loadAdminToken ();

        driver.get ( resolve ( "/login" ) );
        Assert.assertEquals ( resolve ( "/login" ), driver.getCurrentUrl () );

        driver.findElementById ( "email" ).sendKeys ( "admin" );
        driver.findElementById ( "password" ).sendKeys ( adminToken );

        driver.findElementById ( "command" ).submit ();

        // will go back to /setup since we are in maintenance mode

        new WebDriverWait ( driver, 5 ).until ( new Predicate<WebDriver> () {

            @Override
            public boolean apply ( final WebDriver input )
            {
                return driver.getCurrentUrl ().equals ( resolve ( "/setup" ) );
            }
        } );

        // now perform the configuration

        driver.get ( resolve ( "/config" ) );
        Assert.assertEquals ( resolve ( "/config" ), driver.getCurrentUrl () );

        final Select select = new Select ( driver.findElementById ( "jdbcDriver" ) );
        select.selectByValue ( "com.mysql.jdbc.Driver" );

        driver.findElementById ( "url" ).sendKeys ( getTestJdbcUrl () );
        driver.findElementById ( "user" ).sendKeys ( getTestUser () );
        driver.findElementById ( "password" ).sendKeys ( getTestPassword () );

        driver.findElementById ( "command" ).submit ();

        // wait for the JPA unit to be configured

        new WebDriverWait ( driver, 5 ).until ( new Predicate<WebDriver> () {

            @Override
            public boolean apply ( final WebDriver input )
            {
                return driver.findElementById ( "install-schema" ) != null;
            }
        } );

        // get the install schema button and execute

        {
            final WebElement button = driver.findElementById ( "install-schema" );
            Assert.assertNotNull ( button );
            button.click ();
        }

        // wait for the upgrade to finish

        new WebDriverWait ( driver, 5 ).until ( new Predicate<WebDriver> () {

            @Override
            public boolean apply ( final WebDriver input )
            {
                return driver.findElementByTagName ( "h1" ).getText ().equalsIgnoreCase ( "Database Upgrade" );
            }
        } );

        // click to go back

        driver.findElementByCssSelector ( ".btn-default" ).click ();

        // storage service should be present

        new WebDriverWait ( driver, 5 ).until ( new Predicate<WebDriver> () {

            @Override
            public boolean apply ( final WebDriver input )
            {
                return driver.findElementById ( "storage-service-present" ).getText ().equals ( "true" );
            }
        } );

        // install schema should be gone

        final List<WebElement> buttons = driver.findElementsById ( "install-schema" );
        Assert.assertTrue ( "Install schema should be gone", buttons.isEmpty () );
    }

    private static String loadAdminToken ()
    {
        final Properties p = new Properties ();
        try
        {
            p.load ( new FileInputStream ( System.getProperty ( "user.home" ) + "/.drone-admin-token" ) );
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
