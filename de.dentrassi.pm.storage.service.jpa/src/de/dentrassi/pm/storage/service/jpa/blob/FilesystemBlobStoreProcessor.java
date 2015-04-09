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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
import java.util.UUID;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.ByteStreams;

import de.dentrassi.pm.common.utils.ThrowingConsumer;
import de.dentrassi.pm.core.CoreService;

public class FilesystemBlobStoreProcessor
{
    private final static Logger logger = LoggerFactory.getLogger ( FilesystemBlobStoreProcessor.class );

    /**
     * The number of directory levels
     * <p>
     * If we change this, then existing artifacts will not be found anymore
     * </p>
     */
    private static final int LEVELS = 3;

    private final String storeId;

    private final File location;

    private final File data;

    private final File incoming;

    /**
     * Internal state if the blob store is valid
     */
    private boolean valid;

    public FilesystemBlobStoreProcessor ( final File location, final String id )
    {
        this.storeId = id;
        this.location = location;
        this.data = new File ( location, "data" );
        this.incoming = new File ( location, "incoming" );

        logger.info ( "Opening file store - storeId: {}, location: {}", id, location );

        try
        {
            validate ();
        }
        catch ( final Exception e )
        {
            logger.warn ( "Failed to validate store", e );
        }
    }

    public String getStoreId ()
    {
        return this.storeId;
    }

    public void close ()
    {
        logger.info ( "Closing file store - storeId: {}", this.storeId );
    }

    /**
     * Check if the blob store is valid
     * <p>
     * Once the blob store was validated the will be assumed that this does not
     * change.
     * </p>
     */
    private void validate ()
    {
        if ( this.valid )
        {
            return;
        }

        synchronized ( this )
        {
            if ( this.valid )
            {
                return;
            }

            try
            {
                isValid ();
                this.valid = true;
                return;
            }
            catch ( final Exception e )
            {
                throw new IllegalStateException ( "Blob store is not valid. Please check the logs!", e );
            }
        }
    }

    private void isValid () throws IOException
    {

        final Properties p = new Properties ();

        try ( InputStream stream = new FileInputStream ( new File ( this.location, "config.properties" ) ) )
        {
            p.load ( stream );
        }

        final String storedId = p.getProperty ( "id" );
        if ( storedId == null )
        {
            logger.warn ( "Blob store has not a valid id" );
            throw new IllegalStateException ( "Blob store has not a valid id" );
        }

        if ( !storedId.equals ( this.storeId ) )
        {
            logger.warn ( "Store ID does not match - expected: {}, actual: {}", this.storeId, storedId );
            throw new IllegalStateException ( String.format ( "Store ID does not match - expected: %s, actual: %s", this.storeId, storedId ) );
        }
    }

    public void storeBlob ( final InputStream stream, final Function<Long, String> idFunction ) throws IOException
    {
        this.incoming.mkdirs ();

        final Path tmp = Files.createTempFile ( this.incoming.toPath (), null, null );
        try
        {
            // copy to incoming
            long size;
            try ( OutputStream tmpStream = new BufferedOutputStream ( new FileOutputStream ( tmp.toFile () ) ) )
            {
                size = ByteStreams.copy ( stream, tmpStream );
            }

            // get ID for artifact
            final String id = idFunction.apply ( size );

            final File target = makeFileName ( id );

            target.getParentFile ().mkdirs ();

            Files.move ( tmp, target.toPath (), StandardCopyOption.ATOMIC_MOVE );
        }
        finally
        {
            Files.deleteIfExists ( tmp );
        }
    }

    public void deleteBlob ( final String id ) throws IOException
    {
        final File file = makeFileName ( id );
        if ( file.exists () )
        {
            file.delete ();
            // we don't clear up empty directories
        }
    }

    public boolean streamArtifact ( final String id, final ThrowingConsumer<InputStream> consumer ) throws IOException
    {
        final File file = makeFileName ( id );
        if ( !isValid ( file ) )
        {
            return false;
        }

        try ( InputStream stream = new BufferedInputStream ( new FileInputStream ( file ) ) )
        {
            try
            {
                consumer.accept ( stream );
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

        return true;
    }

    // ==>> helper methods

    private boolean isValid ( final File file )
    {
        return file.canRead ();
    }

    private File makeFileName ( final String id )
    {
        final String[] dirs = split ( id );

        File file = this.data;
        for ( final String dir : dirs )
        {
            file = new File ( file, dir );
        }
        return new File ( file, id );
    }

    private String[] split ( String id )
    {
        /*
         * We currently only expect UUIDs and split them like:
         * 9F2EDD76-D2DE-11E4-9897-0BC8CD3138B8 -> 9 / 9F / 9F2
         *
         * this will give a total on each level of:
         * 16 - 256 - 4096
         *
         * So assuming we really have an even distribution of IDs, and we
         * want a limit of 1000 files per leaf directory, we could have
         * about 4 million artifacts in the database.
         */

        id = id.toUpperCase ();

        if ( id.length () < LEVELS )
        {
            // fall back
            return new String[] { id };
        }

        final String[] result = new String[LEVELS];

        final StringBuilder sb = new StringBuilder ();
        for ( int i = 0; i < LEVELS; i++ )
        {
            sb.append ( id.charAt ( i ) );
            result[i] = sb.toString ();
        }

        return result;
    }

    // ==>> static public methods

    public static FilesystemBlobStoreProcessor create ( final File location ) throws IOException
    {
        final File cfgFile = new File ( location, "config.properties" );

        if ( cfgFile.exists () )
        {
            throw new IllegalStateException ( String.format ( "'%s' already contains a store", location ) );
        }

        cfgFile.getParentFile ().mkdirs ();

        final String id = UUID.randomUUID ().toString ().toUpperCase ();

        final Properties p = new Properties ();
        p.put ( "id", id );
        try ( FileOutputStream stream = new FileOutputStream ( cfgFile ) )
        {
            p.store ( stream, null );
        }

        return new FilesystemBlobStoreProcessor ( location, id );
    }

    public static FilesystemBlobStoreProcessor createOrLoad ( final File location, final CoreService service ) throws IOException
    {
        final String id = service.getCoreProperty ( "blobStoreId" );

        if ( id == null )
        {
            return create ( location );
        }
        else
        {
            final FilesystemBlobStoreProcessor result = new FilesystemBlobStoreProcessor ( location, id );
            try
            {
                result.validate ();
            }
            catch ( final Exception e )
            {
                throw new IOException ( "Failed to validate blob store", e );
            }
            return result;
        }
    }
}
