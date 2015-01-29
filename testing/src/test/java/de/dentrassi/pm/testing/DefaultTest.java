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

public class DefaultTest extends AbstractServerTest
{

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
