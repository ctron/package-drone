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
package de.dentrassi.pm.storage.service.jpa.blob;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Function;

import javax.persistence.EntityManager;

import com.google.common.io.ByteStreams;

import de.dentrassi.pm.common.utils.ThrowingConsumer;
import de.dentrassi.pm.storage.jpa.ArtifactEntity;

public class DatabaseBlobStoreProcessor
{

    public void internalStreamArtifactStream ( final EntityManager em, final ArtifactEntity ae, final ThrowingConsumer<InputStream> receiver ) throws SQLException, IOException
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
                catch ( final IOException e )
                {
                    throw e;
                }
                catch ( final Exception e )
                {
                    throw new RuntimeException ( e );
                }
            }
        }
    }

    public void internalStreamArtifactBlob ( final EntityManager em, final ArtifactEntity ae, final ThrowingConsumer<InputStream> receiver ) throws SQLException, IOException
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
                    receiver.accept ( stream );
                }
                catch ( final IOException e )
                {
                    throw e;
                }
                catch ( final Exception e )
                {
                    throw new RuntimeException ( e );
                }
                finally
                {
                    blob.free ();
                }

            }
        }
    }

    public void internalStreamArtifact ( final EntityManager em, final ArtifactEntity ae, final ThrowingConsumer<InputStream> receiver ) throws SQLException, IOException
    {
        if ( Boolean.getBoolean ( "drone.binary.storage.jdbc.read.blobMode" ) )
        {
            internalStreamArtifactBlob ( em, ae, receiver );
        }
        else
        {
            internalStreamArtifactStream ( em, ae, receiver );
        }
    }

    protected void writeBlobAsBlob ( final EntityManager em, final InputStream stream, final Function<Long, String> idFunction ) throws SQLException, IOException
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
            final String id = idFunction.apply ( size );

            try ( PreparedStatement ps = c.prepareStatement ( "update ARTIFACTS set data=? where id=?" ) )
            {
                ps.setBlob ( 1, blob );
                ps.setString ( 2, id );
                ps.executeUpdate ();
            }
        }
        finally
        {
            blob.free ();
        }
    }

    protected void writeBlobAsStream ( final EntityManager em, final InputStream stream, final Function<Long, String> idFunction ) throws SQLException, IOException
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

            final String id = idFunction.apply ( size );

            try ( InputStream in = new FileInputStream ( tmp.toFile () ) )
            {
                try ( PreparedStatement ps = c.prepareStatement ( "update ARTIFACTS set data=? where id=?" ) )
                {
                    ps.setBinaryStream ( 1, in, size );
                    ps.setString ( 2, id );
                    ps.executeUpdate ();
                }
            }
        }
        finally
        {
            Files.deleteIfExists ( tmp );
        }
    }

    public void storeBlob ( final EntityManager em, final InputStream stream, final Function<Long, String> idFunction ) throws SQLException, IOException
    {
        if ( Boolean.getBoolean ( "drone.binary.storage.jdbc.write.blobMode" ) )
        {
            writeBlobAsBlob ( em, stream, idFunction );
        }
        else
        {
            writeBlobAsStream ( em, stream, idFunction );
        }

    }

}
