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
package de.dentrassi.pm.testing;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.common.base.Predicate;

public class FileStorageTest extends AbstractServerTest
{

    @Test
    public void testSetup () throws IOException
    {
        clearOldStorage ();

        final String full = resolve ( "/system/storage" );
        driver.get ( full );
        Assert.assertEquals ( full, driver.getCurrentUrl () );

        final WebElement locationInput = driver.findElement ( By.id ( "location" ) );
        locationInput.sendKeys ( getStoreLocation ().getAbsolutePath () );
        driver.findElement ( By.id ( "fs-convert" ) ).click ();

        new WebDriverWait ( driver, 5 ).until ( new Predicate<WebDriver> () {

            @Override
            public boolean apply ( final WebDriver input )
            {
                // search for "success" button
                return !driver.findElementsById ( "fs-submit" ).isEmpty ();
            }
        } );

        final WebElement button = driver.findElementById ( "fs-submit" );
        Assert.assertTrue ( button.isDisplayed () );
        button.click ();

        new WebDriverWait ( driver, 5 ).until ( new Predicate<WebDriver> () {

            @Override
            public boolean apply ( final WebDriver input )
            {
                return driver.getCurrentUrl ().equals ( resolve ( "/system/storage/fileStore" ) );
            }
        } );

        driver.findElementByClassName ( "alert-success" );
    }

    private void clearOldStorage () throws IOException
    {
        final File file = getStoreLocation ();
        if ( file.isDirectory () )
        {
            System.out.println ( "Clearing old storage: " + file );
            FileUtils.deleteDirectory ( file );
        }
    }
}
