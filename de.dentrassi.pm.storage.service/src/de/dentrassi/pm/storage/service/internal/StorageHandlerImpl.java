/*******************************************************************************
 * Copyright (c) 2014 Jens Reimann.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jens Reimann - initial API and implementation
 *******************************************************************************/
package de.dentrassi.pm.storage.service.internal;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.eclipse.persistence.config.QueryHints;
import org.eclipse.persistence.config.QueryType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.ByteStreams;

import de.dentrassi.pm.aspect.ChannelAspect;
import de.dentrassi.pm.aspect.ChannelAspectProcessor;
import de.dentrassi.pm.aspect.extract.Extractor;
import de.dentrassi.pm.aspect.listener.ChannelListener;
import de.dentrassi.pm.aspect.virtual.Virtualizer;
import de.dentrassi.pm.common.ArtifactInformation;
import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.generator.GenerationContext;
import de.dentrassi.pm.generator.GeneratorProcessor;
import de.dentrassi.pm.storage.StorageAccessor;
import de.dentrassi.pm.storage.jpa.ArtifactEntity;
import de.dentrassi.pm.storage.jpa.AttachedArtifactEntity;
import de.dentrassi.pm.storage.jpa.ChannelEntity;
import de.dentrassi.pm.storage.jpa.GeneratedArtifactEntity;
import de.dentrassi.pm.storage.jpa.GeneratorArtifactEntity;
import de.dentrassi.pm.storage.jpa.StoredArtifactEntity;
import de.dentrassi.pm.storage.jpa.VirtualArtifactEntity;

public class StorageHandlerImpl implements StorageAccessor, StreamServiceHelper
{

    private final static Logger logger = LoggerFactory.getLogger ( StorageHandlerImpl.class );

    private final EntityManager em;

    private final GeneratorProcessor generatorProcessor;

    private final ChannelAspectProcessor channelAspectProcessor = Activator.getChannelAspects ();

    public StorageHandlerImpl ( final EntityManager em, final GeneratorProcessor generatorProcessor )
    {
        this.em = em;
        this.generatorProcessor = generatorProcessor;
    }

    protected ChannelEntity getCheckedChannel ( final String channelId )
    {
        final ChannelEntity channel = this.em.find ( ChannelEntity.class, channelId );
        if ( channel == null )
        {
            throw new IllegalArgumentException ( String.format ( "Channel %s unknown", channelId ) );
        }
        return channel;
    }

    protected ArtifactEntity getCheckedArtifact ( final String artifactId )
    {
        final ArtifactEntity artifact = this.em.find ( ArtifactEntity.class, artifactId );
        if ( artifact == null )
        {
            throw new IllegalArgumentException ( String.format ( "Artifact %s unknown", artifactId ) );
        }
        return artifact;
    }

    @Override
    public void updateChannel ( final String channelId, final String name )
    {
        final ChannelEntity channel = getCheckedChannel ( channelId );

        if ( "".equals ( name ) )
        {
            channel.setName ( null );
        }
        else
        {
            channel.setName ( name );
        }

        this.em.persist ( channel );
    }

    @Override
    public void regenerateAll ( final String channelId )
    {
        scanArtifacts ( channelId, ( ae ) -> {
            if ( ae instanceof GeneratorArtifactEntity )
            {
                try
                {
                    regenerateArtifact ( (GeneratorArtifactEntity)ae );
                }
                catch ( final Exception e )
                {
                    throw new RuntimeException ( e );
                }
            }
        } );
    }

    public void regenerateArtifact ( final GeneratorArtifactEntity ae ) throws Exception
    {
        doStreamed ( this.em, ae, ( file ) -> {

            // first clear old generated artifacts
            deleteGeneratedChildren ( ae );
            this.em.flush ();

            generateArtifact ( ae.getChannel (), ae, file );
            this.em.flush ();
        } );
    }

    protected void deleteGeneratedChildren ( final GeneratorArtifactEntity ae )
    {
        final Query q = this.em.createQuery ( String.format ( "DELETE from %s ga where ga.parent=:parent", GeneratedArtifactEntity.class.getSimpleName () ) );
        q.setParameter ( "parent", ae );
        q.executeUpdate ();
    }

    protected void deleteVirtualChildren ( final ArtifactEntity artifact )
    {
        final Query q = this.em.createQuery ( String.format ( "DELETE from %s va where va.parent=:parent", VirtualArtifactEntity.class.getSimpleName () ) );
        q.setParameter ( "parent", artifact );
        q.executeUpdate ();
    }

    public void generateArtifact ( final ChannelEntity channel, final GeneratorArtifactEntity ae, final Path file ) throws Exception
    {
        final String generatorId = ae.getGeneratorId ();
        final ArtifactContextImpl ctx = createGeneratedContext ( this.em, channel, ae, file );
        this.generatorProcessor.process ( generatorId, ctx );
    }

    private class ArtifactContextImpl implements Virtualizer.Context, GenerationContext
    {
        private final ChannelEntity channel;

        private final Path file;

        private final ArtifactInformation info;

        private final EntityManager em;

        private final Supplier<ArtifactEntity> entitySupplier;

        private ArtifactContextImpl ( final ChannelEntity channel, final Path file, final ArtifactInformation info, final EntityManager em, final Supplier<ArtifactEntity> entitySupplier )
        {
            this.channel = channel;
            this.file = file;
            this.info = info;
            this.em = em;
            this.entitySupplier = entitySupplier;
        }

        @Override
        public ArtifactInformation getOtherArtifactInformation ( final String artifactId )
        {
            if ( artifactId == null )
            {
                return null;
            }

            return convert ( this.em.find ( ArtifactEntity.class, artifactId ) );
        }

        @Override
        public ArtifactInformation getArtifactInformation ()
        {
            return this.info;
        }

        @Override
        public Path getFile ()
        {
            return this.file;
        }

        @Override
        public void createVirtualArtifact ( final String name, final InputStream stream, final Map<MetaKey, String> providedMetaData )
        {
            try
            {
                performStoreArtifact ( this.channel, name, stream, this.em, this.entitySupplier, providedMetaData );
            }
            catch ( final Exception e )
            {
                throw new RuntimeException ( e );
            }
            finally
            {
                try
                {
                    stream.close ();
                }
                catch ( final IOException e )
                {
                    // ignore this one
                }
            }
        }

        @Override
        public StorageAccessor getStorage ()
        {
            return StorageHandlerImpl.this;
        }
    }

    private ArtifactContextImpl createGeneratedContext ( final EntityManager em, final ChannelEntity channel, final ArtifactEntity artifact, final Path file )
    {
        final ArtifactInformation info = convert ( artifact );
        return new ArtifactContextImpl ( channel, file, info, em, ( ) -> {
            final GeneratedArtifactEntity ge = new GeneratedArtifactEntity ();
            ge.setParent ( artifact );
            return ge;
        } );
    }

    public ArtifactEntity performStoreArtifact ( final ChannelEntity channel, final String name, final InputStream stream, final EntityManager em, final Supplier<ArtifactEntity> entityCreator, final Map<MetaKey, String> providedMetaData ) throws Exception
    {
        final Path file = Files.createTempFile ( "blob-", null );

        try
        {
            // copy data to temp file
            try ( BufferedOutputStream os = new BufferedOutputStream ( new FileOutputStream ( file.toFile () ) ) )
            {
                ByteStreams.copy ( stream, os );
            }

            {
                final PreAddContentImpl context = new PreAddContentImpl ( name, file, channel.getId () );
                runChannelTriggers ( channel, listener -> listener.artifactPreAdd ( context ), null );
                if ( context.isVeto () )
                {
                    logger.info ( "Veto add artifact {} to channel {}", name, channel.getId () );
                    return null;
                }
            }

            final Map<MetaKey, String> metadata = extractMetaData ( em, channel, file );

            ArtifactEntity ae;
            try ( BufferedInputStream in = new BufferedInputStream ( new FileInputStream ( file.toFile () ) ) )
            {
                ae = storeBlob ( entityCreator, em, channel, name, in, metadata, providedMetaData );
            }

            if ( ae instanceof GeneratorArtifactEntity )
            {
                generateArtifact ( channel, (GeneratorArtifactEntity)ae, file );
            }
            createVirtualArtifacts ( channel, ae, file );

            final ArtifactInformation a = convert ( ae );

            // now run the post add trigger
            final AddedContextImpl ctx = new AddedContextImpl ( a, metadata, file, this );
            runChannelTriggers ( channel, listener -> listener.artifactAdded ( ctx ), ctx );

            return ae;
        }
        finally
        {
            try
            {
                // delete the temp file, if possible
                Files.deleteIfExists ( file );
            }
            catch ( final Exception e )
            {
                logger.info ( "Failed to delete temp file", e );
                // ignore this
            }
        }
    }

    protected void runChannelTriggers ( final ChannelEntity channel, final Consumer<ChannelListener> listener, final Object event )
    {
        Activator.getChannelAspects ().process ( channel.getAspects (), ChannelAspect::getChannelListener, listener );
        if ( event != null )
        {
            runGeneratorTriggers ( channel, event );
        }
    }

    protected Map<MetaKey, String> extractMetaData ( final EntityManager em, final ChannelEntity channel, final Path file )
    {
        final Map<MetaKey, String> metadata = new HashMap<> ();

        Activator.getChannelAspects ().process ( channel.getAspects (), ChannelAspect::getExtractor, extractor -> {
            try
            {
                final Map<String, String> md = new HashMap<> ();
                extractor.extractMetaData ( file, md );

                convertMetaDataFromExtractor ( metadata, extractor, md );
            }
            catch ( final Exception e )
            {
                throw new RuntimeException ( e );
            }
        } );

        return metadata;
    }

    private void convertMetaDataFromExtractor ( final Map<MetaKey, String> metadata, final Extractor extractor, final Map<String, String> md )
    {
        final String ns = extractor.getAspect ().getId ();
        for ( final Map.Entry<String, String> mde : md.entrySet () )
        {
            metadata.put ( new MetaKey ( ns, mde.getKey () ), mde.getValue () );
        }
    }

    protected ArtifactEntity storeBlob ( final Supplier<ArtifactEntity> artifactSupplier, final EntityManager em, final ChannelEntity channel, final String name, final InputStream stream, final Map<MetaKey, String> extractedMetaData, final Map<MetaKey, String> providedMetaData ) throws SQLException, IOException
    {
        final ArtifactEntity artifact = artifactSupplier.get ();
        artifact.setName ( name );
        artifact.setChannel ( channel );
        artifact.setCreationTimestamp ( new Date () );

        Helper.convertExtractedProperties ( extractedMetaData, artifact, artifact.getExtractedProperties () );
        Helper.convertProvidedProperties ( providedMetaData, artifact, artifact.getProvidedProperties () );

        // set the blob

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

        return artifact;
    }

    private void createVirtualArtifacts ( final ChannelEntity channel, final ArtifactEntity artifact, final Path file )
    {
        Activator.getChannelAspects ().processWithAspect ( channel.getAspects (), ChannelAspect::getArtifactVirtualizer, ( aspect, virtualizer ) -> virtualizer.virtualize ( createArtifactContext ( this.em, channel, artifact, file, aspect.getId () ) ) );
    }

    private ArtifactContextImpl createArtifactContext ( final EntityManager em, final ChannelEntity channel, final ArtifactEntity artifact, final Path file, final String namespace )
    {
        final ArtifactInformation info = convert ( artifact );
        return new ArtifactContextImpl ( channel, file, info, em, ( ) -> {
            final VirtualArtifactEntity ve = new VirtualArtifactEntity ();
            ve.setParent ( artifact );
            ve.setNamespace ( namespace );
            return ve;
        } );
    }

    public void generateArtifact ( final String id )
    {
        try
        {
            final ArtifactEntity ae = this.em.find ( ArtifactEntity.class, id );
            if ( ae == null )
            {
                throw new IllegalArgumentException ( String.format ( "Unable to find artifact '%s' ", id ) );
            }
            if ( ! ( ae instanceof GeneratorArtifactEntity ) )
            {
                throw new IllegalArgumentException ( String.format ( "Artifact '%s' is not a generator artifact.", id ) );
            }

            regenerateArtifact ( (GeneratorArtifactEntity)ae );
        }
        catch ( final Exception e )
        {
            throw new RuntimeException ( e );
        }
    }

    public ArtifactInformation deleteArtifact ( final String artifactId )
    {
        final ArtifactEntity ae = this.em.find ( ArtifactEntity.class, artifactId );
        if ( ae == null )
        {
            return null; // silently ignore
        }

        if ( ae instanceof VirtualArtifactEntity && ( (VirtualArtifactEntity)ae ).getParent () != null )
        {
            throw new IllegalStateException ( String.format ( "Unable to delete virtual artifact of %s (%s)", ae.getName (), ae.getId () ) );
        }

        if ( ae instanceof GeneratedArtifactEntity && ( (GeneratedArtifactEntity)ae ).getParent () != null )
        {
            throw new IllegalStateException ( String.format ( "Unable to delete generated artifact of %s (%s)", ae.getName (), ae.getId () ) );
        }

        final ArtifactInformation info = convert ( ae );

        final String name = ae.getName ();
        final Map<MetaKey, String> metadata = convertMetaData ( ae );

        final ChannelEntity channel = ae.getChannel ();

        this.em.remove ( ae );
        this.em.flush ();

        // now run the post add trigger

        final RemovedContextImpl ctx = new RemovedContextImpl ( artifactId, name, metadata, this, channel.getId () );
        runChannelTriggers ( channel, listener -> listener.artifactRemoved ( ctx ), ctx );

        return info;
    }

    private void runGeneratorTriggers ( final ChannelEntity channel, final Object event )
    {
        scanArtifacts ( channel, ae -> {
            if ( ae instanceof GeneratorArtifactEntity )
            {
                final String gid = ( (GeneratorArtifactEntity)ae ).getGeneratorId ();
                logger.debug ( "Checking generator artifact {} / {} if regeneration is required", ae.getId (), gid );
                this.generatorProcessor.process ( gid, ( generator ) -> {
                    if ( generator.shouldRegenerate ( event ) )
                    {
                        logger.debug ( "Need to re-generate artifact {} with generator {}", ae.getId (), gid );
                        try
                        {
                            regenerateArtifact ( (GeneratorArtifactEntity)ae );
                        }
                        catch ( final Exception e )
                        {
                            throw new RuntimeException ( e );
                        }
                    };
                } );
            }
        } );
    }

    protected void reprocessAspect ( final ChannelEntity channel, final String aspectFactoryId ) throws Exception
    {
        logger.info ( "Reprocessing aspect - channelId: {}, aspect: {}", channel.getId (), aspectFactoryId );

        // first delete all virtual artifacts

        deleteAllVirtualArtifacts ( channel );

        // process new meta data

        for ( final ArtifactEntity ae : channel.getArtifacts () )
        {
            if ( ae instanceof GeneratorArtifactEntity )
            {
                continue;
            }

            logger.debug ( "Reprocessing artifact - {}", ae.getId () );

            doStreamed ( this.em, ae, ( file ) -> {
                // generate metadata for new factory

                final Map<MetaKey, String> metadata = new HashMap<> ();
                final List<String> list = Arrays.asList ( aspectFactoryId );
                this.channelAspectProcessor.process ( list, ChannelAspect::getExtractor, extractor -> {
                    try
                    {
                        final Map<String, String> md = new HashMap<> ();
                        extractor.extractMetaData ( file, md );
                        convertMetaDataFromExtractor ( metadata, extractor, md );
                    }
                    catch ( final Exception e )
                    {
                        throw new RuntimeException ( e );
                    }
                } );

                // add metadata first, since the virtualizers might need it

                Helper.convertExtractedProperties ( metadata, ae, ae.getExtractedProperties () );

                // process virtual

                // we don't process the virtual aspect here, since we have to re-created the whole virtual artifacts, in case they depend on the metadata

                // this.channelAspectProcessor.process ( list, ChannelAspect::getArtifactVirtualizer, virtualizer -> virtualizer.virtualize ( createArtifactContext ( this.em, channel, ae, file, aspectFactoryId ) ) );

                // store

                this.em.persist ( ae );
                this.em.flush ();
            } );
        }

        // re-create virtual artifacts

        createAllVirtualArtifacts ( channel );
    }

    public void scanArtifacts ( final String channelId, final ThrowingConsumer<ArtifactEntity> consumer )
    {
        final ChannelEntity ce = this.em.find ( ChannelEntity.class, channelId );

        if ( ce == null )
        {
            throw new IllegalArgumentException ( String.format ( "Channel %s not found", channelId ) );
        }

        scanArtifacts ( ce, consumer );
    }

    protected void scanArtifacts ( final ChannelEntity ce, final ThrowingConsumer<ArtifactEntity> consumer )
    {
        final TypedQuery<ArtifactEntity> query = this.em.createQuery ( String.format ( "select a from %s a WHERE a.channel=:channel", ArtifactEntity.class.getName () ), ArtifactEntity.class );
        query.setParameter ( "channel", ce );

        query.setHint ( QueryHints.QUERY_TYPE, QueryType.ReadAll );
        query.setHint ( "eclipselink.batch", "a.channel" );

        /*
        query.setHint ( "eclipselink.join-fetch", "a.channel" );
        query.setHint ( "eclipselink.join-fetch", "a.extractedProperties" );
        query.setHint ( "eclipselink.join-fetch", "a.providedProperties" );

        query.setHint ( QueryHints.QUERY_TYPE, QueryType.ReadAll );
        */

        for ( final ArtifactEntity ae : query.getResultList () )
        {
            try
            {
                consumer.accept ( ae );
            }
            catch ( final Exception e )
            {
                throw new RuntimeException ( e );
            }
        }
    }

    public <T extends Comparable<T>> Set<T> listArtifacts ( final String channelId, final Function<ArtifactEntity, T> mapper )
    {
        final Set<T> result = new TreeSet<> ();
        scanArtifacts ( channelId, ( ae ) -> {
            final T t = mapper.apply ( ae );
            if ( t != null )
            {
                result.add ( t );
            }
        } );
        return result;
    }

    @Override
    public Set<ArtifactInformation> getArtifacts ( final String channelId )
    {
        return listArtifacts ( channelId, ( ae ) -> convert ( ae ) );
    }

    public void recreateVirtualArtifacts ( final ArtifactEntity artifact )
    {
        // delete virtual artifacts

        deleteVirtualChildren ( artifact );

        // recreate

        final ChannelEntity channel = artifact.getChannel ();

        try
        {
            doStreamed ( this.em, artifact, ( file ) -> createVirtualArtifacts ( channel, artifact, file ) );
        }
        catch ( final Exception e )
        {
            throw new RuntimeException ( e );
        }

        this.em.flush ();
    }

    public void recreateAllVirtualArtifacts ( final ChannelEntity channel )
    {
        deleteAllVirtualArtifacts ( channel );
        createAllVirtualArtifacts ( channel );
    }

    private void createAllVirtualArtifacts ( final ChannelEntity channel )
    {
        scanArtifacts ( channel, ( artifact ) -> doStreamed ( this.em, artifact, ( file ) -> createVirtualArtifacts ( channel, artifact, file ) ) );
    }

    private void deleteAllVirtualArtifacts ( final ChannelEntity channel )
    {
        final Query q = this.em.createQuery ( String.format ( "DELETE from %s va where va.channel=:channel", VirtualArtifactEntity.class.getSimpleName () ) );
        q.setParameter ( "channel", channel );
        final int result = q.executeUpdate ();

        logger.info ( "Deleted {} artifacts in channel {}", result, channel.getId () );

        this.em.flush ();
    }

    public ArtifactEntity internalCreateArtifact ( final String channelId, final String name, final Supplier<ArtifactEntity> entityCreator, final InputStream stream, final Map<MetaKey, String> providedMetaData )
    {
        try
        {
            final ChannelEntity channel = getCheckedChannel ( channelId );
            final ArtifactEntity ae = performStoreArtifact ( channel, name, stream, this.em, entityCreator, providedMetaData );
            return ae;
        }
        catch ( final Exception e )
        {
            throw new RuntimeException ( e );
        }
        finally
        {
            // always close the stream we got
            try
            {
                stream.close ();
            }
            catch ( final IOException e )
            {
                throw new RuntimeException ( e );
            }
        }
    }

    public ArtifactEntity createAttachedArtifact ( final String parentArtifactId, final String name, final InputStream stream, final Map<MetaKey, String> providedMetaData )
    {
        final ArtifactEntity parentArtifact = getCheckedArtifact ( parentArtifactId );

        if ( ! ( parentArtifact instanceof StoredArtifactEntity ) )
        {
            throw new IllegalArgumentException ( String.format ( "Parent Artifact '%s' is not a normal stored artifact", parentArtifact ) );
        }

        final ArtifactEntity newArtifact = internalCreateArtifact ( parentArtifact.getChannel ().getId (), name, ( ) -> {
            final AttachedArtifactEntity a = new AttachedArtifactEntity ();
            a.setParent ( parentArtifact );
            return a;
        }, stream, providedMetaData );

        return newArtifact;
    }
}
