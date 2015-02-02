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
package de.dentrassi.pm.storage.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity
@Table ( name = "PROPERTIES" )
public class GlobalPropertyEntity
{
    @Id
    @Column ( name = "\"KEY\"", nullable = false, length = 255 )
    private String key;

    @Lob
    private String value;

    public void setKey ( final String key )
    {
        this.key = key;
    }

    public String getKey ()
    {
        return this.key;
    }

    public void setValue ( final String value )
    {
        this.value = value;
    }

    public String getValue ()
    {
        return this.value;
    }
}
