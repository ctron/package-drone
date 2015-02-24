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
package de.dentrassi.osgi.utils;

public final class Strings
{
    private Strings ()
    {
    }

    public static String hex ( final byte[] digest )
    {
        final StringBuilder sb = new StringBuilder ( digest.length * 2 );

        for ( int i = 0; i < digest.length; i++ )
        {
            sb.append ( String.format ( "%02x", digest[i] & 0xFF ) );
        }

        return sb.toString ();
    }
}
