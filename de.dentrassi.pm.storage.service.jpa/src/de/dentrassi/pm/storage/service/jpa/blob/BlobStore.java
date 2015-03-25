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
package de.dentrassi.pm.storage.service.jpa.blob;

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
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.ByteStreams;

import de.dentrassi.pm.common.utils.ThrowingConsumer;
import de.dentrassi.pm.core.CoreService;
import de.dentrassi.pm.storage.ArtifactReceiver;
import de.dentrassi.pm.storage.jpa.ArtifactEntity;
import de.dentrassi.pm.storage.service.jpa.StreamServiceHelper;

/**
 * Implements a blob store which can work on the database and optionally on the
 * file system
 * <p>
 * This blob store fill fall back from filesystem level to database in order to
 * make the filesystem layer optional and support legacy systems.
 * </p>
 */
public class BlobStore
{
    private final static Logger logger = LoggerFactory.getLogger ( BlobStore.class );

    private CoreService coreService;

    private DatabaseBlobStoreProcessor database;

    private final AtomicReference<FilesystemBlobStoreProcessor> filesystem = new AtomicReference<> ();

    private boolean open;

    public void open ( final CoreService coreService )
    {
        this.database = new DatabaseBlobStoreProcessor ();

        this.coreService = coreService;
        checkOpen ();
    }

    public void close ()
    {
        performClose ();
    }

    private void checkOpen ()
    {
        if ( this.open )
        {
            return;
        }

        synchronized ( this )
        {
            if ( this.open )
            {
                return;
            }

            try
            {
                performOpen ();
                this.open = true;
            }
            catch ( final Exception e )
            {
                logger.warn ( "Failed to open store", e );
            }
        }
    }

    private void performOpen ()
    {
        final Map<String, String> map = this.coreService.getCoreProperties ( "blobStoreLocation", "blobStoreId" );

        final String location = map.get ( "blobStoreLocation" );
        final String id = map.get ( "blobStoreId" );

        logger.info ( "Blob store - location: {}, id: {}", location, id );
        if ( location != null && id != null )
        {
            this.filesystem.set ( new FilesystemBlobStoreProcessor ( new File ( location ), id ) );
        }
    }

    private void performClose ()
    {
        this.open = false;
        final FilesystemBlobStoreProcessor currentFilesystem = this.filesystem.getAndSet ( null );

        if ( currentFilesystem != null )
        {
            currentFilesystem.close ();
        }
    }

    public void setLocation ( final File location ) throws IOException
    {
        logger.info ( "Setting blob store location: {}", location );
        final FilesystemBlobStoreProcessor processor;

        processor = FilesystemBlobStoreProcessor.createOrLoad ( location, this.coreService );

        final Map<String, String> properties = new HashMap<> ();

        properties.put ( "blobStoreLocation", location.getAbsolutePath () );
        properties.put ( "blobStoreId", processor.getStoreId () );

        this.coreService.setProperties ( properties );

        final FilesystemBlobStoreProcessor old = this.filesystem.getAndSet ( processor );

        if ( old != null )
        {
            old.close ();
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

    public void streamArtifact ( final EntityManager em, final ArtifactEntity artifact, final ThrowingConsumer<InputStream> consumer ) throws IOException
    {
        checkOpen ();

        final FilesystemBlobStoreProcessor currentFilesystem = this.filesystem.get ();
        if ( currentFilesystem != null )
        {
            if ( currentFilesystem.streamArtifact ( artifact.getId (), consumer ) )
            {
                return;
            }
        }

        try
        {
            this.database.internalStreamArtifact ( em, artifact, consumer );
        }
        catch ( final SQLException e )
        {
            throw new IOException ( e );
        }
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
        checkOpen ();

        final FilesystemBlobStoreProcessor currentFilesystem = this.filesystem.get ();
        if ( currentFilesystem != null )
        {
            currentFilesystem.storeBlob ( in, idFunction );
        }
        else
        {
            this.database.storeBlob ( em, in, idFunction );
        }
    }

    /**
     * Clean up the BLOB of an already deleted artifact
     *
     * @param id
     */
    public void vacuumArtifact ( final String id ) throws IOException
    {
        logger.debug ( "Vacuuming artifact: {}" );

        checkOpen ();

        // there is no need to delete this at a database level, since the entity containing the BLOB is already deleted

        final FilesystemBlobStoreProcessor currentFilesystem = this.filesystem.get ();
        if ( currentFilesystem != null )
        {
            currentFilesystem.deleteBlob ( id );
        }
    }
}
