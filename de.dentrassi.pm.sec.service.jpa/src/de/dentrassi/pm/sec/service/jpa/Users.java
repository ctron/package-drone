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
package de.dentrassi.pm.sec.service.jpa;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;

import de.dentrassi.pm.sec.DatabaseDetails;
import de.dentrassi.pm.sec.DatabaseUserInformation;
import de.dentrassi.pm.sec.jpa.UserEntity;

public final class Users
{
    private Users ()
    {
    }

    public static DatabaseUserInformation convert ( final UserEntity user )
    {
        return convert ( user, null );
    }

    public static DatabaseUserInformation convert ( final UserEntity user, final String rememberMeToken )
    {
        if ( user == null )
        {
            return null;
        }

        final DatabaseDetails details = new DatabaseDetails ();

        details.setName ( user.getName () );
        details.setEmail ( user.getEmail () );
        details.setRegistrationDate ( user.getRegistrationDate () );

        details.setEmailVerified ( user.isEmailVerified () );
        details.setDeleted ( user.isDeleted () );
        details.setLocked ( user.isLocked () );

        if ( user.getEmailToken () != null )
        {
            details.setEmailTokenDate ( user.getEmailTokenDate () );
        }

        details.setRememberMeToken ( rememberMeToken );

        final String roles = user.getRoles ();
        if ( roles != null )
        {
            final String toks[] = roles.split ( "\\s*,\\s*" );
            details.setRoles ( new HashSet<> ( Arrays.asList ( toks ) ) );
        }

        return new DatabaseUserInformation ( user.getId (), details.getRoles (), details );
    }

    protected static MessageDigest createDigest ()
    {
        try
        {
            return MessageDigest.getInstance ( "SHA-256" );
        }
        catch ( final NoSuchAlgorithmException e )
        {
            throw new IllegalStateException ( String.format ( "Message digest could not be created: SHA-256" ) );
        }
    }

    public static String hashIt ( final String salt, String data )
    {
        data = Normalizer.normalize ( data, Form.NFC );

        final byte[] strData = data.getBytes ( StandardCharsets.UTF_8 );
        final byte[] saltData = salt.getBytes ( StandardCharsets.UTF_8 );

        final byte[] first = new byte[saltData.length + strData.length];
        System.arraycopy ( saltData, 0, first, 0, saltData.length );
        System.arraycopy ( strData, 0, first, saltData.length, strData.length );

        final MessageDigest md = createDigest ();

        byte[] digest = md.digest ( first );
        final byte[] current = new byte[saltData.length + digest.length];

        for ( int i = 0; i < 1000; i++ )
        {
            System.arraycopy ( saltData, 0, current, 0, saltData.length );
            System.arraycopy ( digest, 0, current, saltData.length, digest.length );

            digest = md.digest ( current );
        }

        return Base64.getEncoder ().encodeToString ( digest );
    }

}
