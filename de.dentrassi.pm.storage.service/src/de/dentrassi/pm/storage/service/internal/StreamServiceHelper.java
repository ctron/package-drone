/*******************************************************************************
 * Copyright (c) 2014, 2015 Jens Reimann.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jens Reimann - initial API and implementation
 *******************************************************************************/
package de.dentrassi.pm.storage.service.internal;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.persistence.EntityManager;

import com.google.common.io.ByteStreams;

import de.dentrassi.pm.common.ArtifactInformation;
import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.storage.ArtifactReceiver;
import de.dentrassi.pm.storage.jpa.ArtifactEntity;
import de.dentrassi.pm.storage.jpa.AttachedArtifactEntity;
import de.dentrassi.pm.storage.jpa.ChannelEntity;
import de.dentrassi.pm.storage.jpa.ChildArtifactEntity;
import de.dentrassi.pm.storage.jpa.GeneratorArtifactEntity;
import de.dentrassi.pm.storage.jpa.PropertyEntity;
import de.dentrassi.pm.storage.jpa.StoredArtifactEntity;

public interface StreamServiceHelper
{
    default void doStreamed ( final EntityManager em, final ArtifactEntity ae, final ThrowingConsumer<Path> fileConsumer ) throws Exception
    {
        final Path tmp = Files.createTempFile ( "streamed", null );

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

    default void internalStreamArtifact ( final EntityManager em, final ArtifactEntity ae, final ArtifactReceiver receiver ) throws Exception
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

                final Blob blob = rs.getBlob ( 1 );
                try ( InputStream stream = blob.getBinaryStream () )
                {
                    receiver.receive ( convert ( ae ), stream );
                }
                finally
                {
                    blob.free ();
                }
            }
        }
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

        for ( final PropertyEntity entry : extracted )
        {
            metadata.put ( new MetaKey ( entry.getNamespace (), entry.getKey () ), entry.getValue () );
        }

        for ( final PropertyEntity entry : provided )
        {
            metadata.put ( new MetaKey ( entry.getNamespace (), entry.getKey () ), entry.getValue () );
        }

        return metadata;
    }

    default ArtifactInformation convert ( final ArtifactEntity ae )
    {
        if ( ae == null )
        {
            return null;
        }

        return new ArtifactInformation ( ae.getId (), getParentId ( ae ), ae.getSize (), ae.getName (), ae.getChannel ().getId (), ae.getCreationTimestamp (), getArtifactFacets ( ae ), convertMetaData ( ae ) );
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

        if ( ae instanceof AttachedArtifactEntity || ae instanceof StoredArtifactEntity && ! ( ae instanceof GeneratorArtifactEntity ) )
        {
            result.add ( "parentable" );
        }

        return result;
    }

    default boolean isDeleteable ( final ArtifactEntity ae )
    {
        return ae instanceof AttachedArtifactEntity || ae instanceof StoredArtifactEntity;
    }

    default String getParentId ( final ArtifactEntity ae )
    {
        String parentId = null;
        if ( ae instanceof ChildArtifactEntity )
        {
            final ArtifactEntity parent = ( (ChildArtifactEntity)ae ).getParent ();
            if ( parent != null )
            {
                parentId = parent.getId ();
            }
        }
        return parentId;
    }

}
