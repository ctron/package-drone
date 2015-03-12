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

package de.dentrassi.pm.storage.web.channel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import de.dentrassi.pm.common.ChannelAspectInformation;

/**
 * More UI suited channel aspect information
 */
public class AspectInformation
{
    private static final Comparator<AspectInformation> NAME_COMPARATOR = new Comparator<AspectInformation> () {

        @Override
        public int compare ( final AspectInformation o1, final AspectInformation o2 )
        {
            final int rc = o1.getName ().compareTo ( o2.getName () );
            if ( rc != 0 )
            {
                return rc;
            }

            return o1.getFactoryId ().compareTo ( o2.getFactoryId () );
        }
    };

    private final ChannelAspectInformation information;

    private List<AspectInformation> requires = Collections.emptyList ();

    public AspectInformation ( final ChannelAspectInformation information )
    {
        this.information = information;
    }

    public String getName ()
    {
        return this.information.getLabel ();
    }

    public String getFactoryId ()
    {
        return this.information.getFactoryId ();
    }

    public ChannelAspectInformation getInformation ()
    {
        return this.information;
    }

    public boolean isResolved ()
    {
        return this.information.isResolved ();
    }

    public List<AspectInformation> getRequires ()
    {
        return this.requires;
    }

    public static List<AspectInformation> resolve ( final Collection<ChannelAspectInformation> aspects )
    {
        if ( aspects == null )
        {
            return null;
        }

        final Map<String, AspectInformation> map = new HashMap<> ( aspects.size () );

        // convert

        for ( final ChannelAspectInformation aspect : aspects )
        {
            map.put ( aspect.getFactoryId (), new AspectInformation ( aspect ) );
        }

        // then resolve dependencies

        final List<AspectInformation> result = new ArrayList<> ( aspects.size () );

        for ( final AspectInformation info : map.values () )
        {
            info.resolveDeps ( map );
            result.add ( info );
        }

        Collections.sort ( result, NAME_COMPARATOR );

        return result;

    }

    private void resolveDeps ( final Map<String, AspectInformation> map )
    {
        if ( this.information.getRequires () == null )
        {
            this.requires = Collections.emptyList ();
            return;
        }

        this.requires = new ArrayList<> ( this.information.getRequires ().size () );
        for ( final String req : this.information.getRequires () )
        {
            AspectInformation reqInfo = map.get ( req );
            if ( reqInfo == null )
            {
                reqInfo = new AspectInformation ( ChannelAspectInformation.unresolved ( req ) );
            }
            this.requires.add ( reqInfo );
        }

        Collections.sort ( this.requires, NAME_COMPARATOR );
    }

    /**
     * Filter the provided aspect lists by a predicate on the ID
     *
     * @param list
     *            the list to filter
     * @param predicate
     *            the ID predicate
     * @return the filtered list, returns only <code>null</code> when the list
     *         was <code>null</code>
     */
    public static List<AspectInformation> filterIds ( final List<AspectInformation> list, final Predicate<String> predicate )
    {
        if ( list == null )
        {
            return null;
        }

        return list.stream ().filter ( ( i ) -> predicate.test ( i.getFactoryId () ) ).collect ( Collectors.toList () );
    }

    public String[] getMissingIds ( final List<AspectInformation> assignedAspects )
    {
        final Set<String> result = new HashSet<> ();

        for ( final AspectInformation req : this.requires )
        {
            if ( !assignedAspects.contains ( req ) )
            {
                result.add ( req.getFactoryId () );
            }
        }

        return result.toArray ( new String[result.size ()] );
    }

    @Override
    public int hashCode ()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( getFactoryId () == null ? 0 : getFactoryId ().hashCode () );
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
        if ( ! ( obj instanceof AspectInformation ) )
        {
            return false;
        }
        final AspectInformation other = (AspectInformation)obj;
        if ( getFactoryId () == null )
        {
            if ( other.getFactoryId () != null )
            {
                return false;
            }
        }
        else if ( !getFactoryId ().equals ( other.getFactoryId () ) )
        {
            return false;
        }
        return true;
    }

}
