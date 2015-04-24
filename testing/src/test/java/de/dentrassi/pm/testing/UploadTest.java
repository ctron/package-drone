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
import java.io.InputStream;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class UploadTest extends AbstractServerTest
{
    private static final String ROW_ID_PREFIX = "row-";

    @Test
    public void test () throws Exception
    {
        driver.get ( resolve ( "/channel" ) );

        Assert.assertTrue ( getChannels ().isEmpty () );

        driver.get ( resolve ( "/channel/create" ) );

        final List<String> channels = getChannels ();
        Assert.assertEquals ( 1, channels.size () );

        withChannel ( channels.get ( 0 ) );
        basicWithChannel ( channels.get ( 0 ) );
    }

    private void basicWithChannel ( final String channelId ) throws Exception
    {
        testUrl ( "/channel/" + channelId + "/description" );
        testUrl ( "/channel/" + channelId + "/help.maven" );
    }

    protected void testUrl ( final String suffix ) throws Exception
    {
        final URL url = new URL ( resolve ( suffix ) );
        try ( InputStream is = url.openStream () )
        {
        }
    }

    private void withChannel ( final String channelId )
    {
        driver.get ( resolve ( String.format ( "/channel/%s/viewPlain", channelId ) ) );

        // should be an empty channel

        Assert.assertTrue ( findArtifacts ().isEmpty () );

        driver.get ( resolve ( String.format ( "/channel/%s/add", channelId ) ) );

        // test for "Upload" active

        final WebElement link = driver.findElement ( By.linkText ( "Upload" ) );
        Assert.assertTrue ( link.findElement ( By.xpath ( ".." ) ).getAttribute ( "class" ).contains ( "active" ) );;

        // upload file

        final File input = new File ( getAbsolutePath ( "data/test.bundle1-1.0.0-SNAPSHOT.jar" ) );

        {
            final WebElement ele = driver.findElementById ( "file" );
            Assert.assertNotNull ( ele );

            ele.sendKeys ( input.toString () );

            ele.submit ();
        }

        // navigate to plain view

        driver.get ( resolve ( String.format ( "/channel/%s/viewPlain", channelId ) ) );

        // check upload

        final List<String> arts = findArtifacts ();
        Assert.assertEquals ( 1, arts.size () );

        final File file = makeStoreFile ( arts.get ( 0 ) );

        System.out.println ( "Looking for: " + file );

        Assert.assertTrue ( file.exists () );
        Assert.assertTrue ( file.isFile () );
        Assert.assertTrue ( file.canRead () );
        Assert.assertEquals ( input.length (), file.length () );
    }

    private final int LEVEL = 3;

    private File makeStoreFile ( final String id )
    {
        final StringBuilder sb = new StringBuilder ();

        File file = new File ( getStoreLocation (), "data" );

        for ( int i = 0; i < this.LEVEL; i++ )
        {
            sb.append ( id.charAt ( i ) );
            file = new File ( file, sb.toString () );
        }

        return new File ( file, id );
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
