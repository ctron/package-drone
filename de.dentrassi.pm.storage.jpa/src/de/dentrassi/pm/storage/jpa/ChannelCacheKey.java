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

public class ChannelCacheKey implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String channel;

    private String namespace;

    private String key;

    public void setChannel ( final String channelId )
    {
        this.channel = channelId;
    }

    public String getChannel ()
    {
        return this.channel;
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
        result = prime * result + ( this.channel == null ? 0 : this.channel.hashCode () );
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
        if ( ! ( obj instanceof ChannelCacheKey ) )
        {
            return false;
        }
        final ChannelCacheKey other = (ChannelCacheKey)obj;
        if ( this.channel == null )
        {
            if ( other.channel != null )
            {
                return false;
            }
        }
        else if ( !this.channel.equals ( other.channel ) )
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

    @Override
    public String toString ()
    {
        return String.format ( "[channel: %s, namespace: %s, key: %s]", this.channel, this.namespace, this.key );
    }
}
