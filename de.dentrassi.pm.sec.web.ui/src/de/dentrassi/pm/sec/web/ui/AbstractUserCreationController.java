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

import de.dentrassi.osgi.web.controller.validator.ControllerValidator;
import de.dentrassi.osgi.web.controller.validator.ValidationContext;
import de.dentrassi.pm.sec.CreateUser;
import de.dentrassi.pm.sec.DatabaseUserInformation;
import de.dentrassi.pm.sec.UserStorage;

public class AbstractUserCreationController
{
    protected UserStorage storage;

    public void setStorage ( final UserStorage storage )
    {
        this.storage = storage;
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
    }

}
