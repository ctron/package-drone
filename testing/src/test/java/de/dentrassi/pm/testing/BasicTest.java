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

import java.io.InputStream;
import java.net.URL;

import org.junit.Test;

public class BasicTest extends AbstractServerTest
{
    @Test
    public void test1 () throws Exception
    {
        testUrl ( "/" );
    }

    @Test
    public void test2 () throws Exception
    {
        testUrl ( "/p2" );
    }

    @Test
    public void test3 () throws Exception
    {
        testUrl ( "/r5" );
    }

    @Test
    public void test4 () throws Exception
    {
        testUrl ( "/maven" );
    }

    protected void testUrl ( final String suffix ) throws Exception
    {
        final URL url = new URL ( resolve ( suffix ) );
        try ( InputStream is = url.openStream () )
        {
        }
    }
}
