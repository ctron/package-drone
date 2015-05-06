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
package de.dentrassi.pm.storage.jpa;

import static javax.persistence.EnumType.STRING;

import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.UuidGenerator;

@Entity
@Table ( name = "VALIDATION_MESSAGES" )
@UuidGenerator ( name = "CHANVAL_UUID_GEN" )
@Inheritance ( strategy = InheritanceType.SINGLE_TABLE )
@DiscriminatorColumn ( name = "\"TYPE\"", discriminatorType = DiscriminatorType.STRING, length = 1 )
public abstract class ValidationMessageEntity
{
    @Id
    @Column ( name = "ID", nullable = false )
    @GeneratedValue ( generator = "CHANVAL_UUID_GEN" )
    private String id;

    @ManyToOne ( optional = false )
    private ChannelEntity channel;

    @OneToMany
    @JoinTable ( name = "VAL_MSG_ARTIFACTS",
            joinColumns = @JoinColumn ( name = "MSG_ID", referencedColumnName = "ID" ) ,
            inverseJoinColumns = @JoinColumn ( name = "ARTIFACT_ID", referencedColumnName = "ID" ) )
    private Set<ArtifactEntity> artifacts;

    @Column ( name = "NS", nullable = false )
    private String namespace;

    @Basic ( optional = false )
    @Column ( nullable = false )
    @Enumerated ( STRING )
    private ValidationSeverity severity;

    @Lob
    private String message;

    public String getId ()
    {
        return this.id;
    }

    public void setId ( final String id )
    {
        this.id = id;
    }

    public ChannelEntity getChannel ()
    {
        return this.channel;
    }

    public void setChannel ( final ChannelEntity channel )
    {
        this.channel = channel;
    }

    public Set<ArtifactEntity> getArtifacts ()
    {
        return this.artifacts;
    }

    public void setArtifacts ( final Set<ArtifactEntity> artifacts )
    {
        this.artifacts = artifacts;
    }

    public String getNamespace ()
    {
        return this.namespace;
    }

    public void setNamespace ( final String namespace )
    {
        this.namespace = namespace;
    }

    public ValidationSeverity getSeverity ()
    {
        return this.severity;
    }

    public void setSeverity ( final ValidationSeverity severity )
    {
        this.severity = severity;
    }

    public String getMessage ()
    {
        return this.message;
    }

    public void setMessage ( final String message )
    {
        this.message = message;
    }
}
