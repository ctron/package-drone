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

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

public class DatabaseDetails
{
    private String name;

    @Email
    @NotEmpty
    private String email;

    private boolean emailVerified;

    private boolean locked;

    private boolean deleted;

    private Date registrationDate;

    private Date emailTokenDate;

    private String rememberMeToken;

    private Set<String> roles = new HashSet<> ();

    public void setRoles ( final Set<String> roles )
    {
        this.roles = roles;
    }

    public Set<String> getRoles ()
    {
        return this.roles;
    }

    public void setRememberMeToken ( final String rememberMeToken )
    {
        this.rememberMeToken = rememberMeToken;
    }

    public String getRememberMeToken ()
    {
        return this.rememberMeToken;
    }

    public void setEmailTokenDate ( final Date emailTokenDate )
    {
        this.emailTokenDate = emailTokenDate;
    }

    public Date getEmailTokenDate ()
    {
        return this.emailTokenDate;
    }

    public void setRegistrationDate ( final Date registrationDate )
    {
        this.registrationDate = registrationDate;
    }

    public Date getRegistrationDate ()
    {
        return this.registrationDate;
    }

    public void setName ( final String name )
    {
        this.name = name;
    }

    public String getName ()
    {
        return this.name;
    }

    public String getEmail ()
    {
        return this.email;
    }

    public void setEmail ( final String email )
    {
        this.email = email;
    }

    public boolean isEmailVerified ()
    {
        return this.emailVerified;
    }

    public void setEmailVerified ( final boolean emailVerfied )
    {
        this.emailVerified = emailVerfied;
    }

    public boolean isLocked ()
    {
        return this.locked;
    }

    public void setLocked ( final boolean locked )
    {
        this.locked = locked;
    }

    public boolean isDeleted ()
    {
        return this.deleted;
    }

    public void setDeleted ( final boolean deleted )
    {
        this.deleted = deleted;
    }
}
