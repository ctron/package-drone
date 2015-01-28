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
package de.dentrassi.pm.sec.service;

import de.dentrassi.pm.sec.UserInformation;

public interface SecurityService
{
    public UserInformation login ( final String username, final String password ) throws LoginException;

    public UserInformation login ( final String username, final String password, boolean rememberMe ) throws LoginException;

    /**
     * If possible it refreshes the user details from its source <br>
     * If this is not possible it simply returns the inbound value
     *
     * @param user
     *            the user details to refresh
     * @return the, possibly, refreshed user details
     */
    public UserInformation refresh ( UserInformation user );
}
