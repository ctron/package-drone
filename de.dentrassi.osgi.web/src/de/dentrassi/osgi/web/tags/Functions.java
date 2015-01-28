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
package de.dentrassi.osgi.web.tags;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.servlet.http.HttpServletRequest;

import de.dentrassi.osgi.utils.Strings;

public class Functions
{
    private static final MessageDigest MD;

    static
    {
        MessageDigest md = null;
        try
        {
            md = MessageDigest.getInstance ( "MD5" );
        }
        catch ( final NoSuchAlgorithmException e )
        {
        }
        MD = md;
    };

    public static String active ( final HttpServletRequest request, final String targetUrl )
    {
        if ( targetUrl == null )
        {
            return "";
        }

        return request.getServletPath ().equals ( targetUrl ) ? "active" : "";
    }

    public static String toFirstUpper ( final String string )
    {
        if ( string == null || string.isEmpty () )
        {
            return string;
        }

        return string.substring ( 0, 1 ).toUpperCase () + string.substring ( 1 );
    }

    public static String gravatar ( final String email )
    {
        if ( email == null || MD == null )
        {
            return null;
        }

        if ( email.isEmpty () )
        {
            return null;
        }

        try
        {
            return Strings.hex ( MD.digest ( email.getBytes ( "CP1252" ) ) );
        }
        catch ( final UnsupportedEncodingException e )
        {
            return null;
        }
    }
}
