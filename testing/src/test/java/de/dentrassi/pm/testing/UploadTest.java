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

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.WebElement;

public class UploadTest extends AbstractServerTest
{
    private static final String ROW_ID_PREFIX = "row-";

    @Test
    public void test ()
    {
        driver.get ( resolve ( "/channel" ) );

        Assert.assertTrue ( getChannels ().isEmpty () );

        driver.get ( resolve ( "/channel/create" ) );

        final List<String> channels = getChannels ();
        Assert.assertEquals ( 1, channels.size () );

        withChannel ( channels.get ( 0 ) );
    }

    private void withChannel ( final String channelId )
    {
        driver.get ( resolve ( String.format ( "/channel/%s/view", channelId ) ) );

        // should be an empty channel

        Assert.assertTrue ( findArtifacts ().isEmpty () );

        driver.get ( resolve ( String.format ( "/channel/%s/add", channelId ) ) );

        // upload file

        {
            final WebElement ele = driver.findElementById ( "file" );
            Assert.assertNotNull ( ele );

            ele.sendKeys ( getAbsolutePath ( "data/test.bundle1-1.0.0-SNAPSHOT.jar" ) );

            ele.submit ();
        }

        // check upload

        Assert.assertEquals ( 1, findArtifacts ().size () );
    }

    private String getAbsolutePath ( final String localPath )
    {
        final File file = new File ( localPath );
        if ( !file.exists () )
        {
            throw new IllegalStateException ( String.format ( "Unable to find file: %s", localPath ) );
        }
        return file.getAbsolutePath ();
    }

    protected List<String> getChannels ()
    {
        final List<String> result = new LinkedList<> ();

        for ( final WebElement ele : driver.findElementsByCssSelector ( ".channel-id > a" ) )
        {
            result.add ( ele.getText () );
        }

        return result;
    }

    protected List<String> findArtifacts ()
    {
        final List<String> result = new LinkedList<> ();

        for ( final WebElement ele : driver.findElementsByTagName ( "tr" ) )
        {
            final String id = ele.getAttribute ( "id" );
            System.out.println ( "Entry: " + id );
            if ( id == null || !id.startsWith ( ROW_ID_PREFIX ) )
            {
                continue;
            }

            result.add ( id.substring ( ROW_ID_PREFIX.length () ) );
        }

        return result;
    }
}
