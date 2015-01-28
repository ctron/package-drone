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
package de.dentrassi.pm.sec;

import java.util.List;

public interface UserStorage
{
    public static final Object ACTION_TAG_USERS = new Object ();

    public List<DatabaseUserInformation> list ( int position, int size );

    public DatabaseUserInformation createUser ( CreateUser data, boolean emailVerified );

    public DatabaseUserInformation getUserDetails ( String userId );

    public DatabaseUserInformation updateUser ( String userId, DatabaseDetails data );

    public String verifyEmail ( String userId, String token );

    public DatabaseUserInformation getUserDetailsByEmail ( String email );

    public String reRequestEmail ( String email );

    public String resetPassword ( String email );

    public String changePassword ( String email, String token, String password );
}
