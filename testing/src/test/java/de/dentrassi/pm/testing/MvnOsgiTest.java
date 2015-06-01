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

public class MvnOsgiTest extends AbstractServerTest
{
    @Test
    public void testRpm1 () throws Exception
    {
        final ChannelTester ct = ChannelTester.create ( getWebContext (), "mvnosgi1" );
        ct.addAspect ( "mvnosgi" );

        {
            final Set<String> result = ct.upload ( "data/test.bundle1-1.0.0-SNAPSHOT.jar" );
            Assert.assertEquals ( 1, result.size () );
        }
        Assert.assertEquals ( 1, ct.getAllArtifactIds ().size () );

        testUrl ( String.format ( "/maven/%s/mvnosgi/test.bundle1/maven-metadata.xml", ct.getId () ) );
    }

}
