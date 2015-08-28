package de.dentrassi.pm.storage.channel.apm.store;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.eclipse.scada.utils.io.RecursiveDeleteVisitor;

import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.common.utils.IOConsumer;

public class CacheStore implements AutoCloseable
{
    public class TransactionImpl implements Transaction
    {
        private Path tmp;

        public TransactionImpl ()
        {
        }

        @Override
        public void put ( final MetaKey key, final IOConsumer<OutputStream> source ) throws IOException
        {
            makeTemp ();

            final Path path = makePath ( this.tmp, key );
            Files.createDirectories ( path.getParent () );

            try ( OutputStream stream = new BufferedOutputStream ( Files.newOutputStream ( path ) ) )
            {
                source.accept ( stream );
            }
        }

        private void makeTemp () throws IOException
        {
            if ( this.tmp == null )
            {
                this.tmp = Files.createTempDirectory ( CacheStore.this.tmpPath, "trans" );
            }
        }

        @Override
        public boolean stream ( final MetaKey key, final IOConsumer<InputStream> consumer ) throws IOException
        {
            if ( streamFrom ( this.tmp, key, consumer ) )
            {
                return true;
            }

            return streamFrom ( CacheStore.this.dataPath, key, consumer );
        }

        @Override
        public void commit ()
        {
            synchronized ( CacheStore.this )
            {
                markFinished ( this );

                if ( this.tmp == null )
                {
                    // no change
                    return;
                }

                Path tmp2 = null;

                try
                {
                    if ( Files.exists ( CacheStore.this.dataPath ) )
                    {
                        tmp2 = Files.createTempDirectory ( this.tmp, "swap" );
                        Files.move ( CacheStore.this.dataPath, tmp2, StandardCopyOption.ATOMIC_MOVE );
                    }

                    Files.move ( this.tmp, CacheStore.this.dataPath, StandardCopyOption.ATOMIC_MOVE );
                }
                catch ( final IOException e )
                {
                    throw new RuntimeException ( e );
                }

                if ( tmp2 != null )
                {
                    try
                    {
                        Files.walkFileTree ( this.tmp, new RecursiveDeleteVisitor () );
                    }
                    catch ( final IOException e )
                    {
                        // we can ignore this one
                    }
                }
            }
        }

        @Override
        public void rollback ()
        {
            synchronized ( CacheStore.this )
            {
                markFinished ( this );

                if ( this.tmp == null )
                {
                    return;
                }

                try
                {
                    Files.walkFileTree ( this.tmp, new RecursiveDeleteVisitor () );
                }
                catch ( final IOException e )
                {
                    throw new RuntimeException ( "Failed to roll back", e );
                }
            }
        }
    }

    public interface Transaction
    {
        public void put ( MetaKey key, IOConsumer<OutputStream> data ) throws IOException;

        public boolean stream ( final MetaKey key, final IOConsumer<InputStream> consumer ) throws IOException;

        public void commit ();

        public void rollback ();
    }

    private Transaction transaction;

    private final Path dataPath;

    private final Path tmpPath;

    public CacheStore ( final Path path ) throws IOException
    {
        this.dataPath = path.resolve ( "data" );
        this.tmpPath = path.resolve ( "tmp" );

        Files.createDirectories ( this.dataPath );
        Files.createDirectories ( this.tmpPath );
    }

    protected void markFinished ( final TransactionImpl transaction )
    {
        if ( transaction != this.transaction )
        {
            throw new IllegalStateException ( "Wrong transaction tries to finish" );
        }
        this.transaction = null;
    }

    @Override
    public synchronized void close ()
    {
        if ( this.transaction != null )
        {
            this.transaction.rollback ();
            this.transaction = null;
        }
    }

    public synchronized Transaction startTransaction ()
    {
        if ( this.transaction != null )
        {
            throw new IllegalStateException ( "Another transaction is already in progress" );
        }

        this.transaction = new TransactionImpl ();
        return this.transaction;
    }

    /**
     * Stream directly from the storage
     */
    public boolean stream ( final MetaKey key, final IOConsumer<InputStream> consumer ) throws IOException
    {
        return streamFrom ( this.dataPath, key, consumer );
    }

    private boolean streamFrom ( final Path basePath, final MetaKey key, final IOConsumer<InputStream> consumer ) throws IOException
    {
        final Path path = makePath ( basePath, key );

        try ( InputStream stream = new BufferedInputStream ( Files.newInputStream ( path ) ) )
        {
            consumer.accept ( stream );
            return true;
        }
        catch ( final NoSuchFileException e )
        {
            return false;
        }
    }

    private static Path makePath ( final Path basePath, final MetaKey key )
    {
        return basePath.resolve ( key.getNamespace () ).resolve ( key.getKey () );
    }

}
