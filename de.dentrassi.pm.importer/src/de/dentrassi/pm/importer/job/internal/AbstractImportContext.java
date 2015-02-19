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
package de.dentrassi.pm.importer.job.internal;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.dentrassi.osgi.job.ErrorInformation;
import de.dentrassi.pm.common.ArtifactInformation;
import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.importer.ImportContext;
import de.dentrassi.pm.importer.job.ImporterResult;
import de.dentrassi.pm.importer.job.ImporterResult.Entry;
import de.dentrassi.pm.storage.Artifact;

public abstract class AbstractImportContext implements ImportContext, AutoCloseable
{
    private interface ImportEntry
    {
        public void close () throws Exception;

        public Map<MetaKey, String> getProvidedMetaData ();

        public String getName ();

        public InputStream openStream () throws Exception;
    }

    private static abstract class AbstractEntry implements ImportEntry
    {

        private final String name;

        private final Map<MetaKey, String> providedMetaData;

        public AbstractEntry ( final String name, final Map<MetaKey, String> providedMetaData )
        {
            this.name = name;
            this.providedMetaData = providedMetaData;
        }

        @Override
        public String getName ()
        {
            return this.name;
        }

        @Override
        public Map<MetaKey, String> getProvidedMetaData ()
        {
            return this.providedMetaData;
        }
    }

    private static class StreamEntry extends AbstractEntry
    {
        private final InputStream stream;

        public StreamEntry ( final InputStream stream, final String name, final Map<MetaKey, String> providedMetaData )
        {
            super ( name, providedMetaData );
            this.stream = stream;
        }

        @Override
        public InputStream openStream ()
        {
            return this.stream;
        }

        @Override
        public void close () throws IOException
        {
            this.stream.close ();
        }
    }

    private static class FileEntry extends AbstractEntry
    {
        private final Path file;

        private final boolean deleteAfterImport;

        private BufferedInputStream stream;

        public FileEntry ( final Path file, final boolean deleteAfterImport, final String name, final Map<MetaKey, String> providedMetaData )
        {
            super ( name, providedMetaData );

            this.file = file;
            this.deleteAfterImport = deleteAfterImport;
        }

        @Override
        public InputStream openStream () throws Exception
        {
            this.stream = new BufferedInputStream ( new FileInputStream ( this.file.toFile () ) );
            return this.stream;
        }

        @Override
        public void close () throws IOException
        {
            try
            {
                if ( this.stream != null )
                {
                    this.stream.close ();
                }
            }
            finally
            {
                if ( this.deleteAfterImport )
                {
                    Files.deleteIfExists ( this.file );
                }
            }
        }
    }

    private final List<ImportEntry> entries = new LinkedList<> ();

    @Override
    public void scheduleImport ( final InputStream stream, final String name, final Map<MetaKey, String> providedMetaData )
    {
        synchronized ( this.entries )
        {
            this.entries.add ( new StreamEntry ( stream, name, providedMetaData ) );
        }
    }

    @Override
    public void scheduleImport ( final Path file, final boolean deleteAfterImport, final String name, final Map<MetaKey, String> providedMetaData )
    {
        synchronized ( this.entries )
        {
            this.entries.add ( new FileEntry ( file, deleteAfterImport, name, providedMetaData ) );
        }
    }

    public ImporterResult process () throws Exception
    {
        final ImporterResult result = new ImporterResult ();

        result.setChannelId ( getChannelId () );

        Exception err = null;
        long bytes = 0;

        for ( final ImportEntry entry : this.entries )
        {
            if ( err == null )
            {
                try
                {
                    final Artifact art = performImport ( entry.openStream (), entry.getName (), entry.getProvidedMetaData () );
                    final ArtifactInformation info = art.getInformation ();

                    bytes += info.getSize ();

                    result.getEntries ().add ( new Entry ( art.getId (), info.getName (), info.getSize () ) );
                }
                catch ( final Exception e )
                {
                    err = e;
                    result.getEntries ().add ( new Entry ( entry.getName (), ErrorInformation.createFrom ( e ) ) );
                }
            }
            else
            {
                result.getEntries ().add ( new Entry ( entry.getName () ) );
            }
        }

        if ( err != null )
        {
            throw err;
        }

        result.setTotalBytes ( bytes );

        return result;
    }

    protected abstract String getChannelId ();

    protected abstract Artifact performImport ( InputStream stream, String name, Map<MetaKey, String> providedMetaData );

    @Override
    public void close () throws Exception
    {
        final LinkedList<Exception> errors = new LinkedList<> ();

        // close all

        for ( final ImportEntry entry : this.entries )
        {
            try
            {
                entry.close ();
            }
            catch ( final Exception e )
            {
                errors.add ( e );
            }
        }

        // throw later

        if ( !errors.isEmpty () )
        {
            final Exception first = errors.pollFirst ();
            for ( final Exception ex : errors )
            {
                first.addSuppressed ( ex );
            }
        }
    }
}
