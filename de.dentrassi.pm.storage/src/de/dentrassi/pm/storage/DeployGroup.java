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
package de.dentrassi.pm.storage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DeployGroup implements Comparable<DeployGroup>
{
    private String id;

    private String name;

    private List<DeployKey> keys = new ArrayList<> ();

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

    public void setKeys ( final List<DeployKey> keys )
    {
        this.keys = keys;
    }

    public List<DeployKey> getKeys ()
    {
        return this.keys;
    }

    @Override
    public int compareTo ( final DeployGroup o )
    {
        return this.id.compareTo ( o.id );
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
        if ( ! ( obj instanceof DeployGroup ) )
        {
            return false;
        }
        final DeployGroup other = (DeployGroup)obj;
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

    public final static Comparator<DeployGroup> NAME_COMPARATOR = new NameComparator ();

    private static class NameComparator implements Comparator<DeployGroup>
    {
        @Override
        public int compare ( final DeployGroup o1, final DeployGroup o2 )
        {
            if ( o1.name == o2.name )
            {
                return o1.id.compareTo ( o2.id );
            }

            if ( o1.name == null )
            {
                return -1;
            }

            if ( o2.name == null )
            {
                return 1;
            }

            return o1.name.compareTo ( o2.name );
        }

    }
}
