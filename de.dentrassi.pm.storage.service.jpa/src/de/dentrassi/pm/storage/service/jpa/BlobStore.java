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

import static de.dentrassi.pm.storage.service.jpa.StreamServiceHelper.convert;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.ByteStreams;

import de.dentrassi.pm.common.utils.ThrowingConsumer;
import de.dentrassi.pm.core.CoreService;
import de.dentrassi.pm.storage.ArtifactReceiver;
import de.dentrassi.pm.storage.jpa.ArtifactEntity;

public class BlobStore
{
    private final static Logger logger = LoggerFactory.getLogger ( BlobStore.class );

    private CoreService coreService;

    private DatabaseBlobStoreProcessor database;

    public void open ( final CoreService coreService )
    {
        this.database = new DatabaseBlobStoreProcessor ();

        this.coreService = coreService;
        performOpen ();
    }

    public void performOpen ()
    {
        final Map<String, String> map = this.coreService.getCoreProperties ( "blobStoreLocation", "blobStoreId" );

        final String location = map.get ( "blobStoreLocation" );
        final String id = map.get ( "blobStoreId" );

        logger.info ( "Blob store - location: {}, id: {}", location, id );
    }

    public void close ()
    {
    }

    public void setLocation ( final File location )
    {
        close ();

        final Map<String, String> properties = new HashMap<> ();

        properties.put ( "blobStoreLocation", location.getAbsolutePath () );
        properties.put ( "blobStoreId", UUID.randomUUID ().toString () );

        this.coreService.setProperties ( properties );
    }

    public void streamArtifact ( final EntityManager em, final ArtifactEntity artifact, final ThrowingConsumer<InputStream> consumer ) throws IOException
    {
        try
        {
            this.database.internalStreamArtifact ( em, artifact, consumer );
        }
        catch ( final SQLException e )
        {
            throw new IOException ( e );
        }
    }

    public void doStreamed ( final EntityManager em, final ArtifactEntity ae, final ThrowingConsumer<Path> fileConsumer ) throws IOException
    {
        final Path tmp = StreamServiceHelper.createTempFile ( ae.getName () );

        try
        {
            try ( OutputStream os = new BufferedOutputStream ( new FileOutputStream ( tmp.toFile () ) ) )
            {
                streamArtifact ( em, ae, ( ai, in ) -> {
                    ByteStreams.copy ( in, os );
                } );
            }

            try
            {
                fileConsumer.accept ( tmp );
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
        finally
        {
            Files.deleteIfExists ( tmp );
        }
    }

    public void streamArtifact ( final EntityManager em, final ArtifactEntity ae, final ArtifactReceiver receiver ) throws IOException
    {
        streamArtifact ( em, ae, ( stream ) -> receiver.receive ( convert ( ae, null ), stream ) );
    }

    /**
     * Store a blob in the blob store
     * <p>
     * The id function is being called with the exact of the blob as input and
     * must return the ID of the artifact in the database. The also requires
     * that the artifact record is already persisted into the database, without
     * the blob attached.
     * </p>
     *
     * @param em
     *            the entity manager to use
     * @param in
     *            the input stream
     * @param idFunction
     *            the id function
     * @throws SQLException
     *             if a database error occurs
     * @throws IOException
     *             if an IO error occurs
     */
    public void storeBlob ( final EntityManager em, final BufferedInputStream in, final Function<Long, String> idFunction ) throws SQLException, IOException
    {
        this.database.storeBlob ( em, in, idFunction );
    }

    /**
     * Clean up the BLOB of an already deleted artifact
     *
     * @param id
     */
    public void vacuumArtifact ( final String id ) throws IOException
    {
        logger.debug ( "Vacuuming artifact: {}" );

        // there is no need to delete this at a database level, since the entity containing the BLOB is already deleted
    }
}
