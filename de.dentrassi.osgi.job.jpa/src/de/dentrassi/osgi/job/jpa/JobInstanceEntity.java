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
package de.dentrassi.osgi.job.jpa;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Version;

import org.eclipse.persistence.annotations.UuidGenerator;

import de.dentrassi.osgi.job.State;

@Entity
@Table ( name = "JOBS" )
@UuidGenerator ( name = "JI_UUID_GEN" )
public class JobInstanceEntity
{
    @Id
    @Column ( length = 36, nullable = false, updatable = false )
    @GeneratedValue ( generator = "JI_UUID_GEN" )
    private String id;

    @Column ( name = "FACTORY_ID", length = 128, nullable = false, updatable = false )
    private String factoryId;

    @Lob
    @Column ( updatable = false )
    private String data;

    @Lob
    private String result;

    @Convert ( converter = StateConverter.class )
    @Column ( nullable = false )
    private State state;

    @Version
    private long version;

    @Lob
    @Column ( name = "ERROR_INFO" )
    private String errorInformation;

    @Column ( length = 255 )
    private String label;

    public void setLabel ( final String label )
    {
        this.label = label;
    }

    public String getLabel ()
    {
        return this.label;
    }

    public void setResult ( final String result )
    {
        this.result = result;
    }

    public String getResult ()
    {
        return this.result;
    }

    public void setFactoryId ( final String factoryId )
    {
        this.factoryId = factoryId;
    }

    public String getFactoryId ()
    {
        return this.factoryId;
    }

    public void setId ( final String id )
    {
        this.id = id;
    }

    public String getId ()
    {
        return this.id;
    }

    public void setData ( final String data )
    {
        this.data = data;
    }

    public String getData ()
    {
        return this.data;
    }

    public void setState ( final State state )
    {
        this.state = state;
    }

    public State getState ()
    {
        return this.state;
    }

    public String getErrorInformation ()
    {
        return this.errorInformation;
    }

    public void setErrorInformation ( final String errorInformation )
    {
        this.errorInformation = errorInformation;
    }
}
