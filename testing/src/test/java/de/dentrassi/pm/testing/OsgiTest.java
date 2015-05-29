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

import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public class OsgiTest extends AbstractServerTest
{
    @Test
    public void testPlainOsgi1 () throws Exception
    {
        final ChannelTester ct = ChannelTester.create ( getWebContext (), "osgi1" );
        ct.addAspect ( "osgi" );

        {
            final Set<String> result = ct.upload ( "data/test.bundle1-1.0.0-SNAPSHOT.jar" );
            Assert.assertEquals ( 1, result.size () );
        }
        Assert.assertEquals ( 1, ct.getAllArtifactIds ().size () );

        // upload the same artifact a second time

        {
            final Set<String> result = ct.upload ( "data/test.bundle1-1.0.0-SNAPSHOT.jar" );
            Assert.assertEquals ( 1, result.size () );
        }
        Assert.assertEquals ( 2, ct.getAllArtifactIds ().size () );

        testUrl ( "/r5/" + ct.getId () );
    }

    @Test
    public void testP2Osgi1 ()
    {
        final ChannelTester ct = ChannelTester.create ( getWebContext (), "osgi2" );
        ct.addAspect ( "osgi" );
        ct.addAspect ( "p2.repo" );
        ct.addAspect ( "p2.metadata" );

        {
            final Set<String> result = ct.upload ( "data/test.bundle1-1.0.0-SNAPSHOT.jar" );
            Assert.assertEquals ( 3, result.size () ); // expect 1 bundle + 2 p2 fragments
        }
        Assert.assertEquals ( 3, ct.getAllArtifactIds ().size () );

        // upload the same artifact a second time

        {
            final Set<String> result = ct.upload ( "data/test.bundle1-1.0.0-SNAPSHOT.jar" );
            Assert.assertEquals ( 3, result.size () ); // expect 1 bundle + 2 p2 fragments
        }
        Assert.assertEquals ( 6, ct.getAllArtifactIds ().size () );
    }

}
