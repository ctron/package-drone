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

import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Supplier;

import javax.persistence.EntityManager;

import com.google.common.collect.Multimap;
import com.google.common.io.ByteStreams;

import de.dentrassi.pm.common.ArtifactInformation;
import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.common.utils.ThrowingConsumer;
import de.dentrassi.pm.storage.ArtifactReceiver;
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
    default Path createTempFile ( String name ) throws IOException
    {
        if ( name != null )
        {
            name = URLEncoder.encode ( name, "UTF-8" );
        }
        return Files.createTempFile ( "blob-", "-" + name );
    }

    default void doStreamed ( final EntityManager em, final ArtifactEntity ae, final ThrowingConsumer<Path> fileConsumer ) throws Exception
    {
        final Path tmp = createTempFile ( ae.getName () );

        try
        {
            try ( OutputStream os = new BufferedOutputStream ( new FileOutputStream ( tmp.toFile () ) ) )
            {
                internalStreamArtifact ( em, ae, ( ai, in ) -> {
                    ByteStreams.copy ( in, os );
                } );
            }

            fileConsumer.accept ( tmp );
        }
        finally
        {
            Files.deleteIfExists ( tmp );
        }
    }

    default void internalStreamArtifact ( final EntityManager em, final ArtifactEntity ae, final ThrowingConsumer<InputStream> receiver ) throws Exception
    {
        final String artifactId = ae.getId ();

        final Connection c = em.unwrap ( Connection.class );
        try ( PreparedStatement ps = c.prepareStatement ( "select DATA from ARTIFACTS where ID=?" ) )
        {
            ps.setObject ( 1, artifactId );
            try ( ResultSet rs = ps.executeQuery () )
            {
                if ( !rs.next () )
                {
                    throw new FileNotFoundException ( String.format ( "Data for artifact '%s' not found", artifactId ) );
                }

                try ( InputStream stream = rs.getBinaryStream ( 1 ) )
                {
                    receiver.accept ( stream );
                }
            }
        }
    }

    default void internalStreamArtifact ( final EntityManager em, final ArtifactEntity ae, final ArtifactReceiver receiver ) throws Exception
    {
        internalStreamArtifact ( em, ae, ( stream ) -> receiver.receive ( convert ( ae, null ), stream ) );
    }

    public static void writeBlobAsBlob ( final EntityManager em, final ArtifactEntity artifact, final InputStream stream ) throws SQLException, IOException
    {
        final Connection c = em.unwrap ( Connection.class );

        long size;

        final Blob blob = c.createBlob ();
        try
        {
            try ( OutputStream s = blob.setBinaryStream ( 1 ) )
            {
                size = ByteStreams.copy ( stream, s );
            }

            // we can only set it now, since we only have the size
            artifact.setSize ( size );
            em.persist ( artifact );
            em.flush ();

            try ( PreparedStatement ps = c.prepareStatement ( "update ARTIFACTS set data=? where id=?" ) )
            {
                ps.setBlob ( 1, blob );
                ps.setString ( 2, artifact.getId () );
                ps.executeUpdate ();
            }
        }
        finally
        {
            blob.free ();
        }
    }

    public static void writeBlobAsStream ( final EntityManager em, final ArtifactEntity artifact, final InputStream stream ) throws SQLException, IOException
    {
        final Connection c = em.unwrap ( Connection.class );

        final long size;

        final Path tmp = Files.createTempFile ( "blob-", null );
        try
        {

            try ( OutputStream os = new FileOutputStream ( tmp.toFile () ) )
            {
                size = ByteStreams.copy ( stream, os );
            }

            artifact.setSize ( size );
            em.persist ( artifact );
            em.flush ();

            try ( InputStream in = new FileInputStream ( tmp.toFile () ) )
            {
                try ( PreparedStatement ps = c.prepareStatement ( "update ARTIFACTS set data=? where id=?" ) )
                {
                    ps.setBinaryStream ( 1, in, size );
                    ps.setString ( 2, artifact.getId () );
                    ps.executeUpdate ();
                }
            }
        }
        finally
        {
            Files.deleteIfExists ( tmp );
        }

    }

    default ArtifactEntity storeBlob ( final Supplier<ArtifactEntity> artifactSupplier, final EntityManager em, final ChannelEntity channel, final String name, final InputStream stream, final Map<MetaKey, String> extractedMetaData, final Map<MetaKey, String> providedMetaData ) throws SQLException, IOException
    {
        final ArtifactEntity artifact = artifactSupplier.get ();
        artifact.setName ( name );
        artifact.setChannel ( channel );
        artifact.setCreationTimestamp ( new Date () );

        Helper.convertExtractedProperties ( extractedMetaData, artifact, artifact.getExtractedProperties () );
        Helper.convertProvidedProperties ( providedMetaData, artifact, artifact.getProvidedProperties () );

        // set the blob

        if ( Boolean.getBoolean ( "package.drone.binary.streamMode" ) )
        {
            writeBlobAsStream ( em, artifact, stream );
        }
        else
        {
            writeBlobAsBlob ( em, artifact, stream );
        }

        return artifact;
    }

    default SortedMap<MetaKey, String> convertMetaData ( final ArtifactEntity ae )
    {
        return convertMetaData ( ae.getExtractedProperties (), ae.getProvidedProperties () );
    }

    default SortedMap<MetaKey, String> convertMetaData ( final ChannelEntity ce )
    {
        return convertMetaData ( ce.getExtractedProperties (), ce.getProvidedProperties () );
    }

    default SortedMap<MetaKey, String> convertMetaData ( final Collection<? extends PropertyEntity> extracted, final Collection<? extends PropertyEntity> provided )
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

    /**
     * Convert an artifact entity to an detailed artifact information object
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
    default ArtifactInformation convert ( final ArtifactEntity ae, final Multimap<String, MetaDataEntry> properties )
    {
        if ( ae == null )
        {
            return null;
        }

        final SortedSet<String> childIds = new TreeSet<> ();
        for ( final String childId : ae.getChildIds () )
        {
            childIds.add ( childId );
        }

        SortedMap<MetaKey, String> metaData;
        if ( properties != null )
        {
            metaData = extract ( ae.getId (), properties );
        }
        else
        {
            metaData = convertMetaData ( ae );
        }

        return new ArtifactInformation ( ae.getId (), getParentId ( ae ), ae.getSize (), ae.getName (), ae.getChannel ().getId (), ae.getCreationTimestamp (), getArtifactFacets ( ae ), metaData, childIds );
    }

    default SortedMap<MetaKey, String> extract ( final String id, final Multimap<String, MetaDataEntry> properties )
    {
        final SortedMap<MetaKey, String> result = new TreeMap<> ();

        for ( final MetaDataEntry entry : properties.get ( id ) )
        {
            result.put ( entry.getKey (), entry.getValue () );
        }

        return result;
    }

    default Set<String> getArtifactFacets ( final ArtifactEntity ae )
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

    default boolean isDeleteable ( final ArtifactEntity ae )
    {
        return ae instanceof AttachedArtifactEntity || ae instanceof StoredArtifactEntity || ae instanceof GeneratorArtifactEntity;
    }

    default String getParentId ( final ArtifactEntity ae )
    {
        if ( ae instanceof ChildArtifactEntity )
        {
            return ( (ChildArtifactEntity)ae ).getParentId ();
        }
        return null;
    }

    default void testLocked ( final ChannelEntity channel )
    {
        if ( channel.isLocked () )
        {
            throw new ChannelLockedException ( channel.getId () );
        }
    }

}
