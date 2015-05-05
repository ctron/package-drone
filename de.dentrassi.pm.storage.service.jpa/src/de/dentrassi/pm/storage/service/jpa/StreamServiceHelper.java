/*******************************************************************************
 * Copyright (c) 2014, 2015 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package de.dentrassi.pm.storage.service.jpa;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import com.google.common.collect.Multimap;

import de.dentrassi.pm.common.ArtifactInformation;
import de.dentrassi.pm.common.DetailedArtifactInformation;
import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.storage.ChannelLockedException;
import de.dentrassi.pm.storage.jpa.ArtifactEntity;
import de.dentrassi.pm.storage.jpa.AttachedArtifactEntity;
import de.dentrassi.pm.storage.jpa.ChannelEntity;
import de.dentrassi.pm.storage.jpa.ChildArtifactEntity;
import de.dentrassi.pm.storage.jpa.GeneratorArtifactEntity;
import de.dentrassi.pm.storage.jpa.PropertyEntity;
import de.dentrassi.pm.storage.jpa.StoredArtifactEntity;

public interface StreamServiceHelper
{
    public static Path createTempFile ( String name ) throws IOException
    {
        if ( name != null )
        {
            name = URLEncoder.encode ( name, "UTF-8" );
        }
        return Files.createTempFile ( "blob-", "-" + name );
    }

    public static SortedMap<MetaKey, String> convertMetaData ( final ArtifactEntity ae )
    {
        return convertMetaData ( ae.getExtractedProperties (), ae.getProvidedProperties () );
    }

    public static SortedMap<MetaKey, String> convertMetaData ( final ChannelEntity ce )
    {
        return convertMetaData ( ce.getExtractedProperties (), ce.getProvidedProperties () );
    }

    public static SortedMap<MetaKey, String> convertMetaData ( final Collection<? extends PropertyEntity> extracted, final Collection<? extends PropertyEntity> provided )
    {
        final SortedMap<MetaKey, String> metadata = new TreeMap<> ();

        if ( extracted != null )
        {
            for ( final PropertyEntity entry : extracted )
            {
                metadata.put ( new MetaKey ( entry.getNamespace (), entry.getKey () ), entry.getValue () );
            }
        }

        if ( provided != null )
        {
            for ( final PropertyEntity entry : provided )
            {
                metadata.put ( new MetaKey ( entry.getNamespace (), entry.getKey () ), entry.getValue () );
            }
        }

        return metadata;
    }

    public static ChannelImpl convert ( final ChannelEntity ce, final StorageServiceImpl service )
    {
        if ( ce == null )
        {
            return null;
        }
        return new ChannelImpl ( ce.getId (), ce.getName (), ce.getDescription (), ce.isLocked (), ce.getAggregatedNumberOfWarnings (), ce.getAggregatedNumberOfErrors (), service );
    }

    /**
     * Convert an artifact entity to a full artifact information object
     * <p>
     * If there is an additional properties map provided, then the meta data
     * will be used from the properties map. Otherwise the artifact entity will
     * be used as source of properties, which might trigger another select on
     * the database.
     * </p>
     *
     * @param ae
     *            the entity to convert
     * @param props
     *            the optional properties
     * @return the result information object
     */
    public static ArtifactInformation convert ( final ArtifactEntity ae, final Multimap<String, MetaDataEntry> properties )
    {
        if ( ae == null )
        {
            return null;
        }

        final SortedSet<String> childIds = new TreeSet<> ();
        for ( final ChildArtifactEntity child : ae.getChildArtifacts () )
        {
            childIds.add ( child.getId () );
        }

        final SortedMap<MetaKey, String> metaData;
        if ( properties != null )
        {
            metaData = extract ( ae.getId (), properties );
        }
        else
        {
            metaData = convertMetaData ( ae );
        }

        return new ArtifactInformation ( ae.getId (), getParentId ( ae ), ae.getSize (), ae.getName (), ae.getChannel ().getId (), ae.getCreationTimestamp (), ae.getAggregatedNumberOfWarnings (), ae.getAggregatedNumberOfErrors (), getArtifactFacets ( ae ), metaData, childIds );
    }

    /**
     * Convert an artifact entity to a detailed artifact information object
     * <p>
     * If there is an additional properties map provided, then the meta data
     * will be used from the properties map. Otherwise the artifact entity will
     * be used as source of properties, which might trigger another select on
     * the database.
     * </p>
     *
     * @param ae
     *            the entity to convert
     * @param props
     *            the optional properties
     * @return the result information object
     */
    public static DetailedArtifactInformation convertDetailed ( final ArtifactEntity ae, final Multimap<String, MetaDataEntry> properties )
    {
        if ( ae == null )
        {
            return null;
        }

        final SortedMap<MetaKey, String> metaData;
        if ( properties != null )
        {
            metaData = extract ( ae.getId (), properties );
        }
        else
        {
            metaData = convertMetaData ( ae );
        }

        return new DetailedArtifactInformation ( ae.getId (), getParentId ( ae ), ae.getSize (), ae.getName (), ae.getChannel ().getId (), ae.getCreationTimestamp (), ae.getAggregatedNumberOfWarnings (), ae.getAggregatedNumberOfErrors (), getArtifactFacets ( ae ), metaData );
    }

    public static SortedMap<MetaKey, String> extract ( final String id, final Multimap<String, MetaDataEntry> properties )
    {
        final SortedMap<MetaKey, String> result = new TreeMap<> ();

        for ( final MetaDataEntry entry : properties.get ( id ) )
        {
            result.put ( entry.getKey (), entry.getValue () );
        }

        return result;
    }

    public static Set<String> getArtifactFacets ( final ArtifactEntity ae )
    {
        final Set<String> result = new TreeSet<> ();

        if ( ae instanceof GeneratorArtifactEntity )
        {
            result.add ( "generator" );
        }

        if ( isDeleteable ( ae ) )
        {
            result.add ( "deletable" );
        }

        if ( ae instanceof AttachedArtifactEntity || ae instanceof StoredArtifactEntity )
        {
            result.add ( "parentable" );
        }

        return result;
    }

    public static boolean isDeleteable ( final ArtifactEntity ae )
    {
        return ae instanceof AttachedArtifactEntity || ae instanceof StoredArtifactEntity || ae instanceof GeneratorArtifactEntity;
    }

    public static String getParentId ( final ArtifactEntity ae )
    {
        if ( ae instanceof ChildArtifactEntity )
        {
            return ( (ChildArtifactEntity)ae ).getParentId ();
        }
        return null;
    }

    public static void testLocked ( final ChannelEntity channel )
    {
        if ( channel.isLocked () )
        {
            throw new ChannelLockedException ( channel.getId () );
        }
    }

}
