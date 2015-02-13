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
package de.dentrassi.pm.deb;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import de.dentrassi.pm.common.MetaKeyBinding;

public class ChannelConfiguration
{
    @MetaKeyBinding ( namespace = "apt", key = "origin" )
    private String origin;

    @MetaKeyBinding ( namespace = "apt", key = "label" )
    private String label;

    @MetaKeyBinding ( namespace = "apt", key = "suite" )
    private String suite;

    @MetaKeyBinding ( namespace = "apt", key = "version" )
    private String version;

    @MetaKeyBinding ( namespace = "apt", key = "codename" )
    private String codename;

    @MetaKeyBinding ( namespace = "apt", key = "components", converterClass = SpaceJoiner.class )
    private Set<String> components = new HashSet<> ();

    @MetaKeyBinding ( namespace = "apt", key = "defaultComponent" )
    private String defaultComponent;

    @MetaKeyBinding ( namespace = "apt", key = "description" )
    private String description;

    @MetaKeyBinding ( namespace = "apt", key = "architectures", converterClass = SpaceJoiner.class )
    private Set<String> architectures = new HashSet<> ();

    @MetaKeyBinding ( namespace = "apt", key = "distribution" )
    private String distribution;

    public String getOrigin ()
    {
        return this.origin;
    }

    public void setOrigin ( final String origin )
    {
        this.origin = origin;
    }

    public String getLabel ()
    {
        return this.label;
    }

    public String getLabel ( final String defaultValue )
    {
        if ( this.label == null )
        {
            return defaultValue;
        }
        return this.label;
    }

    public void setLabel ( final String label )
    {
        this.label = label;
    }

    public String getSuite ()
    {
        return this.suite;
    }

    public void setSuite ( final String suite )
    {
        this.suite = suite;
    }

    public String getVersion ()
    {
        return this.version;
    }

    public void setVersion ( final String version )
    {
        this.version = version;
    }

    public String getCodename ()
    {
        return this.codename;
    }

    public void setCodename ( final String codename )
    {
        this.codename = codename;
    }

    public Set<String> getComponents ()
    {
        if ( this.components == null )
        {
            return Collections.singleton ( getDefaultComponent () );
        }
        return this.components;
    }

    public void setComponents ( final Set<String> components )
    {
        this.components = components;
    }

    public String getDescription ()
    {
        return this.description;
    }

    public void setDescription ( final String description )
    {
        this.description = description;
    }

    public Set<String> getArchitectures ()
    {
        if ( this.architectures == null )
        {
            return new HashSet<> ( Arrays.asList ( "i386", "amd64" ) );
        }

        return this.architectures;
    }

    public void setArchitectures ( final Set<String> architectures )
    {
        this.architectures = architectures;
    }

    public String getDefaultComponent ()
    {
        if ( this.defaultComponent == null || this.defaultComponent.isEmpty () )
        {
            return "main";
        }
        return this.defaultComponent;
    }

    public void setDefaultComponent ( final String defaultComponent )
    {
        this.defaultComponent = defaultComponent;
    }

    public void setDistribution ( final String distribution )
    {
        this.distribution = distribution;
    }

    public String getDistribution ()
    {
        if ( this.distribution == null || this.distribution.isEmpty () )
        {
            return "default";
        }
        return this.distribution;
    }

    public boolean isValid ()
    {
        return true;
    }
}
