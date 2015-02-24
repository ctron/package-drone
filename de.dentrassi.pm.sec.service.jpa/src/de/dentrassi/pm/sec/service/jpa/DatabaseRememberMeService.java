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

import static de.dentrassi.pm.sec.service.jpa.Users.convert;
import static de.dentrassi.pm.sec.service.jpa.Users.hashIt;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import de.dentrassi.pm.sec.UserInformation;
import de.dentrassi.pm.sec.jpa.UserEntity;
import de.dentrassi.pm.sec.jpa.UserEntity_;
import de.dentrassi.pm.sec.service.LoginException;
import de.dentrassi.pm.sec.service.UserService;

public class DatabaseRememberMeService extends AbstractDatabaseUserService implements UserService
{

    @Override
    public UserInformation refresh ( final UserInformation user )
    {
        return null;
    }

    @Override
    public UserInformation checkCredentials ( final String username, final String credentials, final boolean rememberMe ) throws LoginException
    {
        return doWithTransaction ( ( em ) -> processLogin ( em, username, credentials ) );
    }

    private UserInformation processLogin ( final EntityManager em, final String email, final String credentials ) throws LoginException
    {
        final UserEntity user = findByEmail ( em, email );

        if ( user == null )
        {
            // let the next one try
            return null;
        }

        if ( user.getRememberMeTokenHash () == null || user.getRememberMeTokenSalt () == null )
        {
            // let others try
            return null;
        }

        final String tokenHash = hashIt ( user.getRememberMeTokenSalt (), credentials );
        if ( !tokenHash.equals ( user.getRememberMeTokenHash () ) )
        {
            // wrong token - let the next one try, or fail with a "not found" error
            return null;
        }

        // only fail _after_ the password has been checked, so we should be sure it is the user

        validateUserAfterLogin ( user );

        // we made it

        return convert ( user );
    }

    protected UserEntity findByRememberMeToken ( final EntityManager em, final String tokenHash )
    {
        final CriteriaBuilder cb = em.getCriteriaBuilder ();
        final CriteriaQuery<UserEntity> cq = cb.createQuery ( UserEntity.class );

        final Root<UserEntity> root = cq.from ( UserEntity.class );

        cq.where ( cb.equal ( root.get ( UserEntity_.rememberMeTokenHash ), tokenHash ) );

        final TypedQuery<UserEntity> q = em.createQuery ( cq );

        // limit

        q.setMaxResults ( 1 );

        // execute

        final List<UserEntity> rl = q.getResultList ();

        if ( rl.isEmpty () )
        {
            return null;
        }

        return rl.get ( 0 );
    }

}
