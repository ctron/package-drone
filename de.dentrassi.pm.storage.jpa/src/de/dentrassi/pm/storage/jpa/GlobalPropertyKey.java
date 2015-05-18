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

import java.io.Serializable;

public class GlobalPropertyKey implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String namespace;

    private String key;

    public GlobalPropertyKey ( final String namespace, final String key )
    {
        this.namespace = namespace;
        this.key = key;
    }

    public GlobalPropertyKey ()
    {
    }

    public void setNamespace ( final String namespace )
    {
        this.namespace = namespace;
    }

    public void setKey ( final String key )
    {
        this.key = key;
    }

    public String getNamespace ()
    {
        return this.namespace;
    }

    public String getKey ()
    {
        return this.key;
    }

    @Override
    public int hashCode ()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( this.key == null ? 0 : this.key.hashCode () );
        result = prime * result + ( this.namespace == null ? 0 : this.namespace.hashCode () );
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
        if ( ! ( obj instanceof GlobalPropertyKey ) )
        {
            return false;
        }
        final GlobalPropertyKey other = (GlobalPropertyKey)obj;
        if ( this.key == null )
        {
            if ( other.key != null )
            {
                return false;
            }
        }
        else if ( !this.key.equals ( other.key ) )
        {
            return false;
        }
        if ( this.namespace == null )
        {
            if ( other.namespace != null )
            {
                return false;
            }
        }
        else if ( !this.namespace.equals ( other.namespace ) )
        {
            return false;
        }
        return true;
    }
}
