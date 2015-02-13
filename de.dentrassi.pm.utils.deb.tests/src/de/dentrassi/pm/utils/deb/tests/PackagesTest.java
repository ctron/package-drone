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
package de.dentrassi.pm.utils.deb.tests;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.SortedMap;

import org.junit.Assert;
import org.junit.Test;

import de.dentrassi.pm.utils.deb.Packages;

public class PackagesTest
{
    @Test
    public void test1 () throws IOException, ParseException
    {
        SortedMap<String, String> control;
        try ( InputStream is = PackagesTest.class.getResourceAsStream ( "data/test1" ) )
        {
            control = Packages.parseControlFile ( is );
        }

        final String md5 = Packages.makeDescriptionMd5 ( control.get ( "Description" ) );

        Assert.assertEquals ( "38d96b653196d5ef8c667efe23411a81", md5 );
    }
}
