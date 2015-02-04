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
package de.dentrassi.pm.common.utils;

import java.security.SecureRandom;

import de.dentrassi.osgi.utils.Strings;

public final class Tokens
{
    private static final SecureRandom random = new SecureRandom ();

    private Tokens ()
    {
    }

    public static String createToken ( final int length )
    {
        final byte[] data = new byte[length];

        random.nextBytes ( data );

        return Strings.hex ( data );
    }
}
