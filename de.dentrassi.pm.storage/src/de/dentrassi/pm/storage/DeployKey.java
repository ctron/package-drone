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

import java.util.Comparator;
import java.util.Date;

public class DeployKey
{
    private String id;

    private String groupId;

    private String name;

    private String key;

    private Date creationTimestamp;

    public void setId ( final String id )
    {
        this.id = id;
    }

    public String getId ()
    {
        return this.id;
    }

    public void setGroupId ( final String groupId )
    {
        this.groupId = groupId;
    }

    public String getGroupId ()
    {
        return this.groupId;
    }

    public void setName ( final String name )
    {
        this.name = name;
    }

    public String getName ()
    {
        return this.name;
    }

    public void setCreationTimestamp ( final Date creationTimestamp )
    {
        this.creationTimestamp = creationTimestamp;
    }

    public Date getCreationTimestamp ()
    {
        return this.creationTimestamp;
    }

    public void setKey ( final String key )
    {
        this.key = key;
    }

    public String getKey ()
    {
        return this.key;
    }

    public final static Comparator<DeployKey> NAME_COMPARATOR = new NameComparator ();

    private static class NameComparator implements Comparator<DeployKey>
    {
        @Override
        public int compare ( final DeployKey o1, final DeployKey o2 )
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
