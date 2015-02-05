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
package de.dentrassi.pm.sec.jpa;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.eclipse.persistence.annotations.UuidGenerator;

@Entity
@Table ( name = "USERS" )
@UuidGenerator ( name = "USER_UUID_GEN" )
public class UserEntity
{
    @Id
    @Column ( length = 36, nullable = false )
    @GeneratedValue ( generator = "USER_UUID_GEN" )
    private String id;

    @Column ( length = 256 )
    private String name;

    @Column ( name = "REG_DATE", nullable = false )
    @Temporal ( TemporalType.TIMESTAMP )
    private Date registrationDate;

    @Column ( length = 256, unique = true )
    private String email;

    @Column ( length = 128, name = "EMAIL_TOK_SALT" )
    private String emailTokenSalt;

    @Column ( length = 128, name = "EMAIL_TOK" )
    private String emailToken;

    @Column ( name = "EMAIL_TOK_TS" )
    @Temporal ( TemporalType.TIMESTAMP )
    private Date emailTokenDate;

    @Column ( name = "EMAIL_VERIFIED" )
    private boolean emailVerified;

    @Column ( length = 128, name = "PASSWORD_HASH" )
    private String passwordHash;

    @Column ( length = 128, name = "PASSWORD_SALT" )
    private String passwordSalt;

    @Column
    private boolean deleted;

    @Column
    private boolean locked;

    @Column ( length = 256, name = "REM_TOKEN_HASH" )
    private String rememberMeTokenHash;

    @Column ( length = 128, name = "REM_TOKEN_SALT" )
    private String rememberMeTokenSalt;

    @Column ( name = "ROLES" )
    @Lob
    private String roles;

    public void setRoles ( final String roles )
    {
        this.roles = roles;
    }

    public String getRoles ()
    {
        return this.roles;
    }

    public void setRememberMeTokenHash ( final String rememberMeTokenHash )
    {
        this.rememberMeTokenHash = rememberMeTokenHash;
    }

    public String getRememberMeTokenHash ()
    {
        return this.rememberMeTokenHash;
    }

    public void setRememberMeTokenSalt ( final String rememberMeTokenSalt )
    {
        this.rememberMeTokenSalt = rememberMeTokenSalt;
    }

    public String getRememberMeTokenSalt ()
    {
        return this.rememberMeTokenSalt;
    }

    public void setLocked ( final boolean locked )
    {
        this.locked = locked;
    }

    public boolean isLocked ()
    {
        return this.locked;
    }

    public void setEmailVerified ( final boolean emailVerified )
    {
        this.emailVerified = emailVerified;
    }

    public boolean isEmailVerified ()
    {
        return this.emailVerified;
    }

    public void setDeleted ( final boolean deleted )
    {
        this.deleted = deleted;
    }

    public boolean isDeleted ()
    {
        return this.deleted;
    }

    public void setPasswordSalt ( final String passwordSalt )
    {
        this.passwordSalt = passwordSalt;
    }

    public String getPasswordSalt ()
    {
        return this.passwordSalt;
    }

    public void setPasswordHash ( final String passwordHash )
    {
        this.passwordHash = passwordHash;
    }

    public String getPasswordHash ()
    {
        return this.passwordHash;
    }

    public void setRegistrationDate ( final Date registrationDate )
    {
        this.registrationDate = registrationDate;
    }

    public Date getRegistrationDate ()
    {
        return this.registrationDate;
    }

    public String getEmail ()
    {
        return this.email;
    }

    public void setEmail ( final String email )
    {
        this.email = email;
    }

    public void setId ( final String id )
    {
        this.id = id;
    }

    public String getId ()
    {
        return this.id;
    }

    public void setName ( final String name )
    {
        this.name = name;
    }

    public String getName ()
    {
        return this.name;
    }

    @Override
    public int hashCode ()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( this.id == null ? 0 : this.id.hashCode () );
        return result;
    }

    @Override
    public boolean equals ( final Object obj )
    {
        if ( this == obj )
        {
            return true;
        }
        if ( obj == null )
        {
            return false;
        }
        if ( ! ( obj instanceof UserEntity ) )
        {
            return false;
        }
        final UserEntity other = (UserEntity)obj;
        if ( this.id == null )
        {
            if ( other.id != null )
            {
                return false;
            }
        }
        else if ( !this.id.equals ( other.id ) )
        {
            return false;
        }
        return true;
    }

    public String getEmailTokenSalt ()
    {
        return this.emailTokenSalt;
    }

    public void setEmailTokenSalt ( final String emailTokenSalt )
    {
        this.emailTokenSalt = emailTokenSalt;
    }

    public String getEmailToken ()
    {
        return this.emailToken;
    }

    public void setEmailToken ( final String emailToken )
    {
        this.emailToken = emailToken;
    }

    public Date getEmailTokenDate ()
    {
        return this.emailTokenDate;
    }

    public void setEmailTokenDate ( final Date emailTokenDate )
    {
        this.emailTokenDate = emailTokenDate;
    }

}
