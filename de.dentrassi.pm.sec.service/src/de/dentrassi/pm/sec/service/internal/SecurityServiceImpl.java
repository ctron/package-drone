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
package de.dentrassi.pm.sec.service.internal;

import java.util.Collection;

import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.ServiceTracker;

import de.dentrassi.pm.sec.UserInformation;
import de.dentrassi.pm.sec.service.LoginException;
import de.dentrassi.pm.sec.service.SecurityService;
import de.dentrassi.pm.sec.service.UserService;

public class SecurityServiceImpl implements SecurityService
{
    private final ServiceTracker<UserService, UserService> userServiceTracker;

    public SecurityServiceImpl ()
    {
        this.userServiceTracker = new ServiceTracker<> ( FrameworkUtil.getBundle ( SecurityServiceImpl.class ).getBundleContext (), UserService.class, null );
    }

    public void activate ()
    {
        this.userServiceTracker.open ();
    }

    public void deactivate ()
    {
        this.userServiceTracker.close ();
    }

    @Override
    public UserInformation login ( final String username, final String password ) throws LoginException
    {
        return login ( username, password, false );
    }

    @Override
    public UserInformation login ( final String username, final String password, final boolean rememberMe ) throws LoginException
    {
        final Collection<UserService> services = this.userServiceTracker.getTracked ().values ();

        if ( services == null || services.isEmpty () )
        {
            throw new LoginException ( "No login service available" );
        }

        for ( final UserService service : services )
        {
            final UserInformation user = service.checkCredentials ( username, password, rememberMe );
            if ( user != null )
            {
                return user;
            }
        }

        throw new LoginException ( "Login error!", "Invalid username or password." );
    }

    @Override
    public UserInformation refresh ( final UserInformation user )
    {
        final Collection<UserService> services = this.userServiceTracker.getTracked ().values ();

        if ( services == null || services.isEmpty () )
        {
            return user;
        }

        for ( final UserService service : services )
        {
            final UserInformation refreshedUser = service.refresh ( user );
            if ( refreshedUser != null )
            {
                return refreshedUser;
            }
        }

        return user;
    }
}
