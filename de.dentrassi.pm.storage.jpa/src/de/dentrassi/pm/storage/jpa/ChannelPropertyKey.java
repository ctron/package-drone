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

public class ChannelPropertyKey implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String channelId;

    private String namespace;

    private String key;

    public void setChannelId ( final String channelId )
    {
        this.channelId = channelId;
    }

    public String getChannelId ()
    {
        return this.channelId;
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
        result = prime * result + ( this.channelId == null ? 0 : this.channelId.hashCode () );
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
        if ( ! ( obj instanceof ChannelPropertyKey ) )
        {
            return false;
        }
        final ChannelPropertyKey other = (ChannelPropertyKey)obj;
        if ( this.channelId == null )
        {
            if ( other.channelId != null )
            {
                return false;
            }
        }
        else if ( !this.channelId.equals ( other.channelId ) )
        {
            return false;
        }
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
