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
package de.dentrassi.pm.sec.web.ui;

import de.dentrassi.osgi.web.controller.binding.MessageBindingError;
import de.dentrassi.osgi.web.controller.validator.ControllerValidator;
import de.dentrassi.osgi.web.controller.validator.ValidationContext;
import de.dentrassi.pm.sec.CreateUser;
import de.dentrassi.pm.sec.DatabaseUserInformation;
import de.dentrassi.pm.sec.UserStorage;
import de.dentrassi.pm.sec.service.password.BadPasswordException;
import de.dentrassi.pm.sec.service.password.PasswordChecker;

public class AbstractUserCreationController
{
    protected UserStorage storage;

    protected PasswordChecker passwordChecker;

    public void setStorage ( final UserStorage storage )
    {
        this.storage = storage;
    }

    public void setPasswordChecker ( final PasswordChecker passwordChecker )
    {
        this.passwordChecker = passwordChecker;
    }

    @ControllerValidator ( formDataClass = CreateUser.class )
    public void validateCreateUser ( final CreateUser createUser, final ValidationContext context )
    {
        final DatabaseUserInformation user = this.storage.getUserDetailsByEmail ( createUser.getEmail () );
        if ( user != null )
        {
            context.error ( "email", "A user is already registered for this e-mail address" );
            context.setMarker ( "duplicateEmail" );
        }

        if ( createUser.getPassword () != null && !createUser.getPassword ().isEmpty () )
        {
            checkPassword ( createUser.getPassword (), context );
        }
    }

    @ControllerValidator ( formDataClass = NewPassword.class )
    public void validatePassword ( final NewPassword data, final ValidationContext context )
    {
        checkPassword ( data.getPassword (), context );
    }

    protected void checkPassword ( final String password, final ValidationContext context )
    {
        try
        {
            this.passwordChecker.checkPassword ( password );
        }
        catch ( final BadPasswordException e )
        {
            context.error ( "password", new MessageBindingError ( e.getMessage () ) );
        }
    }

}
