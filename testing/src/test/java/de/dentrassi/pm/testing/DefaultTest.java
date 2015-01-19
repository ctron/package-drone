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

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.common.base.Predicate;

public class DefaultTest extends AbstractServerTest
{

    @Test
    public void testSetup () throws Exception
    {
        TestSuite.getDriver ().get ( getBase () );
        Assert.assertEquals ( resolve ( "/setup" ), driver.getCurrentUrl () );

        final Select select = new Select ( driver.findElementById ( "jdbcDriver" ) );
        select.selectByValue ( "com.mysql.jdbc.Driver" );

        driver.findElementById ( "url" ).sendKeys ( getTestJdbcUrl () );
        driver.findElementById ( "user" ).sendKeys ( getTestUser () );
        driver.findElementById ( "password" ).sendKeys ( getTestPassword () );

        driver.findElementById ( "command" ).submit ();

        {
            final WebElement button = driver.findElementById ( "install-schema" );
            Assert.assertNotNull ( button );
            button.click ();
        }

        new WebDriverWait ( driver, 5 ).until ( new Predicate<WebDriver> () {

            @Override
            public boolean apply ( final WebDriver input )
            {
                return driver.findElementByTagName ( "h1" ).getText ().equalsIgnoreCase ( "Database Upgrade" );
            }
        } );

        driver.findElementByCssSelector ( ".btn-default" ).click ();

        new WebDriverWait ( driver, 5 ).until ( new Predicate<WebDriver> () {

            @Override
            public boolean apply ( final WebDriver input )
            {
                return driver.findElementById ( "service-present" ).getText ().equals ( "true" );
            }
        } );
    }

    @Test
    public void testBase ()
    {
        driver.get ( getBase () );
        Assert.assertEquals ( resolve ( "/" ), driver.getCurrentUrl () );
    }

    @Test
    public void testChannels ()
    {
        driver.get ( resolve ( "/channel" ) );
        Assert.assertEquals ( resolve ( "/channel" ), driver.getCurrentUrl () );
    }
}
