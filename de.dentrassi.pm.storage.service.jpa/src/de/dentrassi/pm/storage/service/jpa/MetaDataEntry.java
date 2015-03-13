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
package de.dentrassi.pm.storage.service.jpa;

import de.dentrassi.pm.common.MetaKey;

public class MetaDataEntry implements Comparable<MetaDataEntry>
{
    private final MetaKey key;

    private final String value;

    public MetaDataEntry ( final MetaKey key, final String value )
    {
        this.key = key;
        this.value = value;
    }

    public MetaKey getKey ()
    {
        return this.key;
    }

    public String getValue ()
    {
        return this.value;
    }

    @Override
    public int hashCode ()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( this.key == null ? 0 : this.key.hashCode () );
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
        if ( ! ( obj instanceof MetaDataEntry ) )
        {
            return false;
        }
        final MetaDataEntry other = (MetaDataEntry)obj;
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
        return true;
    }

    @Override
    public int compareTo ( final MetaDataEntry o )
    {
        return this.key.compareTo ( o.key );
    }

}
