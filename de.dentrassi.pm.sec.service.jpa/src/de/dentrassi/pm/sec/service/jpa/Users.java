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

import java.util.Arrays;
import java.util.HashSet;

import de.dentrassi.pm.sec.DatabaseDetails;
import de.dentrassi.pm.sec.DatabaseDetailsBean;
import de.dentrassi.pm.sec.DatabaseUserInformation;
import de.dentrassi.pm.sec.jpa.UserEntity;

public final class Users extends de.dentrassi.pm.sec.service.common.Users
{
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

        final DatabaseDetailsBean details = new DatabaseDetailsBean ();

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

        final String roles = user.getRoles ();
        if ( roles != null )
        {
            final String toks[] = roles.split ( "\\s*,\\s*" );
            details.setRoles ( new HashSet<> ( Arrays.asList ( toks ) ) );
        }

        return new DatabaseUserInformation ( user.getId (), rememberMeToken, details.getRoles (), new DatabaseDetails ( details ) );
    }
}
