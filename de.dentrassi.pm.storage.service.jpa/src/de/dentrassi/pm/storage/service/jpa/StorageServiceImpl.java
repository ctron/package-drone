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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.function.Supplier;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Multimap;

import de.dentrassi.osgi.web.LinkTarget;
import de.dentrassi.pm.common.ArtifactInformation;
import de.dentrassi.pm.common.ChannelAspectInformation;
import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.common.SimpleArtifactInformation;
import de.dentrassi.pm.common.service.AbstractJpaServiceImpl;
import de.dentrassi.pm.common.utils.ThrowingConsumer;
import de.dentrassi.pm.generator.GeneratorProcessor;
import de.dentrassi.pm.storage.Artifact;
import de.dentrassi.pm.storage.ArtifactReceiver;
import de.dentrassi.pm.storage.CacheEntry;
import de.dentrassi.pm.storage.CacheEntryInformation;
import de.dentrassi.pm.storage.Channel;
import de.dentrassi.pm.storage.DeployGroup;
import de.dentrassi.pm.storage.DeployKey;
import de.dentrassi.pm.storage.jpa.ArtifactEntity;
import de.dentrassi.pm.storage.jpa.ArtifactEntity_;
import de.dentrassi.pm.storage.jpa.ChannelEntity;
import de.dentrassi.pm.storage.jpa.DeployGroupEntity;
import de.dentrassi.pm.storage.jpa.DeployKeyEntity;
import de.dentrassi.pm.storage.jpa.ExtractedArtifactPropertyEntity;
import de.dentrassi.pm.storage.jpa.ExtractedChannelPropertyEntity;
import de.dentrassi.pm.storage.jpa.GeneratorArtifactEntity;
import de.dentrassi.pm.storage.jpa.PropertyEntity;
import de.dentrassi.pm.storage.jpa.ProvidedArtifactPropertyEntity;
import de.dentrassi.pm.storage.jpa.ProvidedChannelPropertyEntity;
import de.dentrassi.pm.storage.jpa.StoredArtifactEntity;
import de.dentrassi.pm.storage.jpa.VirtualArtifactEntity;
import de.dentrassi.pm.storage.service.StorageService;

public class StorageServiceImpl extends AbstractJpaServiceImpl implements StorageService, StreamServiceHelper
{
    private final static Logger logger = LoggerFactory.getLogger ( StorageServiceImpl.class );

    private final GeneratorProcessor generatorProcessor = new GeneratorProcessor ( FrameworkUtil.getBundle ( StorageServiceImpl.class ).getBundleContext () );

    private final LockManager<String> lockManager = new LockManager<> ();

    public void start ()
    {
        this.generatorProcessor.open ();
    }

    public void stop ()
    {
        this.generatorProcessor.close ();
    }

    @Override
    public Channel createChannel ( final String name, final String description )
    {
        return doWithHandler ( ( handler ) -> convert ( handler.createChannel ( name, description, null ) ) );
    }

    protected ChannelImpl convert ( final ChannelEntity channel )
    {
        return convert ( channel, this );
    }

    @Override
    public void deleteChannel ( final String channelId )
    {
        doWithHandlerVoid ( ( storage ) -> storage.deleteChannel ( channelId, true ) );
    }

    @Override
    public Channel getChannel ( final String channelId )
    {
        return doWithTransaction ( em -> {
            final ChannelEntity channel = em.find ( ChannelEntity.class, channelId );
            return convert ( channel );
        } );
    }

    @Override
    public Channel getChannelWithAlias ( final String channelIdOrName )
    {
        return doWithTransaction ( em -> {
            ChannelEntity channel = em.find ( ChannelEntity.class, channelIdOrName );
            if ( channel == null )
            {
                channel = findByName ( em, channelIdOrName );
            }
            return convert ( channel );
        } );
    }

    protected ChannelEntity findByName ( final EntityManager em, final String channelName )
    {
        final TypedQuery<ChannelEntity> q = em.createQuery ( String.format ( "SELECT c FROM %s AS c WHERE c.name=:name", ChannelEntity.class.getName () ), ChannelEntity.class );
        q.setParameter ( "name", channelName );

        // we don't use getSingleResult since it throws an exception if the entry is not found

        final List<ChannelEntity> result = q.getResultList ();
        if ( result.isEmpty () )
        {
            return null;
        }

        return result.get ( 0 );
    }

    @Override
    public Artifact createGeneratorArtifact ( final String channelId, final String name, final String generatorId, final InputStream stream, final Map<MetaKey, String> providedMetaData )
    {
        return internalCreateArtifact ( channelId, name, ( ) -> {
            final GeneratorArtifactEntity gae = new GeneratorArtifactEntity ();
            gae.setGeneratorId ( generatorId );
            return gae;
        }, stream, providedMetaData );
    }

    @Override
    public Artifact createArtifact ( final String channelId, final String name, final InputStream stream, final Map<MetaKey, String> providedMetaData )
    {
        return internalCreateArtifact ( channelId, name, StoredArtifactEntity::new, stream, providedMetaData );
    }

    private Artifact internalCreateArtifact ( final String channelId, final String name, final Supplier<ArtifactEntity> entityCreator, final InputStream stream, final Map<MetaKey, String> providedMetaData )
    {
        return this.lockManager.modifyCall ( channelId, ( ) -> {
            return doWithTransaction ( ( em ) -> {
                final StorageHandlerImpl hi = new StorageHandlerImpl ( em, this.generatorProcessor, this.lockManager );

                final ChannelEntity channel = getCheckedChannel ( em, channelId );

                testLocked ( channel );

                final ArtifactEntity artifact = hi.internalCreateArtifact ( channelId, name, entityCreator, stream, providedMetaData, true );

                if ( artifact == null )
                {
                    return null;
                }

                return convert ( convert ( artifact.getChannel () ), artifact, null );
            } );
        } );
    }

    protected ChannelEntity getCheckedChannel ( final EntityManager em, final String channelId )
    {
        final ChannelEntity channel = em.find ( ChannelEntity.class, channelId );
        if ( channel == null )
        {
            throw new IllegalArgumentException ( String.format ( "Channel %s unknown", channelId ) );
        }
        return channel;
    }

    public Set<Artifact> listArtifacts ( final String channelId )
    {
        return doWithTransaction ( em -> {
            final StorageHandlerImpl hi = new StorageHandlerImpl ( em, this.generatorProcessor, this.lockManager );
            final ChannelEntity ce = hi.getCheckedChannel ( channelId );
            final ChannelImpl channel = convert ( ce );
            final Multimap<String, MetaDataEntry> properties = hi.getChannelArtifactProperties ( ce );
            return hi.<Artifact> listArtifacts ( ce, ( ae ) -> convert ( channel, ae, properties ) );
        } );
    }

    public Set<ArtifactInformation> listArtifactInformations ( final String channelId )
    {
        return doWithHandler ( ( hi ) -> hi.getArtifacts ( channelId ) );
    }

    public Set<SimpleArtifactInformation> listSimpleArtifacts ( final String channelId )
    {
        return doWithHandler ( ( hi ) -> hi.<SimpleArtifactInformation> listArtifacts ( hi.getCheckedChannel ( channelId ), ( ae ) -> convertSimple ( ae ) ) );
    }

    private SimpleArtifactInformation convertSimple ( final ArtifactEntity ae )
    {
        if ( ae == null )
        {
            return null;
        }

        logger.trace ( "Convert to simple: {}", ae.getId () );

        return new SimpleArtifactInformation ( ae.getId (), getParentId ( ae ), ae.getSize (), ae.getName (), ae.getChannel ().getId (), ae.getCreationTimestamp (), getArtifactFacets ( ae ) );
    }

    private Artifact convert ( final ChannelImpl channel, final ArtifactEntity ae, final Multimap<String, MetaDataEntry> properties )
    {
        if ( ae == null )
        {
            return null;
        }

        logger.debug ( "Converting entity: {} / {}", ae.getId (), ae.getClass () );

        if ( logger.isTraceEnabled () )
        {
            final Class<?>[] clsArray = ae.getClass ().getInterfaces ();
            for ( final Class<?> cls : clsArray )
            {
                logger.trace ( "interface: {}", cls );
            }
        }

        if ( ae instanceof GeneratorArtifactEntity )
        {
            final LinkTarget[] targets = new LinkTarget[1];
            this.generatorProcessor.process ( ( (GeneratorArtifactEntity)ae ).getGeneratorId (), ( gen ) -> targets[0] = gen.getEditTarget ( ae.getId () ) );
            return new GeneratorArtifactImpl ( channel, ae.getId (), convert ( ae, properties ), targets[0] );
        }
        else
        {
            return new ArtifactImpl ( channel, ae.getId (), convert ( ae, properties ) );
        }
    }

    @Override
    public void streamArtifact ( final String artifactId, final ArtifactReceiver receiver ) throws FileNotFoundException
    {
        final Boolean found = doWithTransaction ( em -> {
            final ArtifactEntity ae = em.find ( ArtifactEntity.class, artifactId );
            if ( ae == null )
            {
                return false;
            }
            internalStreamArtifact ( em, ae, receiver );
            return true;
        } );

        if ( !found )
        {
            throw new FileNotFoundException ( String.format ( "Artifact '%s' could not be found", artifactId ) );
        }
    }

    private ArtifactEntity getCheckedArtifact ( final EntityManager em, final String artifactId )
    {
        final ArtifactEntity ae = em.find ( ArtifactEntity.class, artifactId );

        if ( ae == null )
        {
            throw new IllegalArgumentException ( String.format ( "Artifact %s not found", artifactId ) );
        }
        return ae;
    }

    @Override
    public Collection<Channel> listChannels ()
    {
        return doWithTransaction ( em -> {
            final CriteriaQuery<ChannelEntity> cq = em.getCriteriaBuilder ().createQuery ( ChannelEntity.class );

            final TypedQuery<ChannelEntity> q = em.createQuery ( cq );
            final List<ChannelEntity> rl = q.getResultList ();

            final List<Channel> result = new ArrayList<> ( rl.size () );
            for ( final ChannelEntity ce : rl )
            {
                result.add ( convert ( ce ) );
            }

            return result;
        } );
    }

    @Override
    public SimpleArtifactInformation deleteArtifact ( final String artifactId )
    {
        return doWithTransaction ( em -> {
            final StorageHandlerImpl hi = new StorageHandlerImpl ( em, this.generatorProcessor, this.lockManager );
            return hi.deleteArtifact ( artifactId );
        } );
    }

    public List<ChannelAspectInformation> getChannelAspectInformations ( final String channelId )
    {
        return doWithTransaction ( em -> {
            final ChannelEntity channel = getCheckedChannel ( em, channelId );
            return Activator.getChannelAspects ().resolve ( channel.getAspects ().keySet () );
        } );
    }

    @Override
    public void addChannelAspect ( final String channelId, final String aspectFactoryId, final boolean withDependencies )
    {
        doWithHandlerVoid ( ( handler ) -> handler.addChannelAspects ( channelId, Collections.singleton ( aspectFactoryId ), withDependencies ) );
    }

    @Override
    public void refreshChannelAspect ( final String channelId, final String aspectFactoryId )
    {
        this.lockManager.modifyRun ( channelId, ( ) -> {
            doWithTransactionVoid ( em -> {

                final ChannelEntity channel = getCheckedChannel ( em, channelId );

                testLocked ( channel );

                if ( channel.getAspects ().containsKey ( aspectFactoryId ) )
                {
                    new StorageHandlerImpl ( em, this.generatorProcessor, this.lockManager ).reprocessAspects ( channel, Collections.singleton ( aspectFactoryId ) );
                }
            } );
        } );
    }

    @Override
    public void refreshAllChannelAspects ( final String channelId )
    {
        this.lockManager.modifyRun ( channelId, ( ) -> {
            doWithTransactionVoid ( em -> {

                final ChannelEntity channel = getCheckedChannel ( em, channelId );

                testLocked ( channel );

                new StorageHandlerImpl ( em, this.generatorProcessor, this.lockManager ).reprocessAspects ( channel, channel.getAspects ().keySet () );
            } );
        } );
    }

    @Override
    public void removeChannelAspect ( final String channelId, final String aspectFactoryId )
    {
        this.lockManager.modifyRun ( channelId, ( ) -> {

            doWithTransactionVoid ( em -> {
                final ChannelEntity channel = getCheckedChannel ( em, channelId );

                testLocked ( channel );

                channel.getAspects ().remove ( aspectFactoryId );
                em.persist ( channel );
                em.flush ();

                {
                    final Query q = em.createQuery ( String.format ( "DELETE from %s ap where ap.namespace=:factoryId and ap.artifact.channel.id=:channelId", ExtractedArtifactPropertyEntity.class.getSimpleName () ) );
                    q.setParameter ( "factoryId", aspectFactoryId );
                    q.setParameter ( "channelId", channelId );
                    q.executeUpdate ();
                }

                {
                    final Query q = em.createQuery ( String.format ( "DELETE from %s ap where ap.namespace=:factoryId and ap.artifact.channel.id=:channelId", ProvidedArtifactPropertyEntity.class.getSimpleName () ) );
                    q.setParameter ( "factoryId", aspectFactoryId );
                    q.setParameter ( "channelId", channelId );
                    q.executeUpdate ();
                }

                {
                    final Query q = em.createQuery ( String.format ( "DELETE from %s cp where cp.namespace=:factoryId and cp.channel.id=:channelId", ExtractedChannelPropertyEntity.class.getSimpleName () ) );
                    q.setParameter ( "factoryId", aspectFactoryId );
                    q.setParameter ( "channelId", channelId );
                    q.executeUpdate ();
                }

                {
                    final Query q = em.createQuery ( String.format ( "DELETE from %s cp where cp.namespace=:factoryId and cp.channel.id=:channelId", ProvidedChannelPropertyEntity.class.getSimpleName () ) );
                    q.setParameter ( "factoryId", aspectFactoryId );
                    q.setParameter ( "channelId", channelId );
                    q.executeUpdate ();
                }

                {
                    final Query q = em.createQuery ( String.format ( "DELETE from %s va where va.namespace=:factoryId and va.channel.id=:channelId", VirtualArtifactEntity.class.getSimpleName () ) );
                    q.setParameter ( "factoryId", aspectFactoryId );
                    q.setParameter ( "channelId", channelId );
                    q.executeUpdate ();
                }

                final StorageHandlerImpl hi = new StorageHandlerImpl ( em, this.generatorProcessor, this.lockManager );

                hi.recreateAllVirtualArtifacts ( channel );
            } );

        } );
    }

    private ArtifactEntity getArtifact ( final EntityManager em, final String artifactId )
    {
        final CriteriaBuilder cb = em.getCriteriaBuilder ();
        final CriteriaQuery<ArtifactEntity> cq = cb.createQuery ( ArtifactEntity.class );

        // query

        final Root<ArtifactEntity> root = cq.from ( ArtifactEntity.class );
        final Predicate where = cb.equal ( root.get ( ArtifactEntity_.id ), artifactId );

        // fetch
        root.fetch ( ArtifactEntity_.channel );
        root.fetch ( ArtifactEntity_.providedProperties, JoinType.LEFT );
        root.fetch ( ArtifactEntity_.extractedProperties, JoinType.LEFT );

        // select

        cq.select ( root ).where ( where );

        // convert

        final TypedQuery<ArtifactEntity> q = em.createQuery ( cq );

        // q.setMaxResults ( 1 );
        final List<ArtifactEntity> rl = q.getResultList ();
        if ( rl.isEmpty () )
        {
            return null;
        }
        else
        {
            return rl.get ( 0 );
            // return em.find ( ArtifactEntity.class, artifactId );
        }
    }

    @Override
    public ArtifactInformation getArtifactInformation ( final String artifactId )
    {
        return doWithTransaction ( em -> convert ( getArtifact ( em, artifactId ), null ) );
    }

    @Override
    public Artifact getArtifact ( final String artifactId )
    {
        return doWithTransaction ( em -> {
            final ArtifactEntity artifact = getArtifact ( em, artifactId );
            if ( artifact == null )
            {
                return null;
            }
            return convert ( convert ( artifact.getChannel () ), artifact, null );
        } );
    }

    public Map<MetaKey, String> applyMetaData ( final String artifactId, final Map<MetaKey, String> metadata )
    {
        return doWithTransaction ( em -> {
            final StorageHandlerImpl hi = new StorageHandlerImpl ( em, this.generatorProcessor, this.lockManager );

            final ArtifactEntity artifact = getCheckedArtifact ( em, artifactId );

            testLocked ( artifact.getChannel () );

            final Map<MetaKey, String> result = convert ( artifact.getProvidedProperties () );

            // merge

            mergeMetaData ( metadata, result );

            // first clear all

            artifact.getProvidedProperties ().clear ();

            em.persist ( artifact );
            em.flush ();

            // now add the new set

            Helper.convertProvidedProperties ( result, artifact, artifact.getProvidedProperties () );

            // store

            em.persist ( artifact );
            em.flush ();

            // recreate virtual artifacts

            hi.recreateVirtualArtifacts ( artifact );

            // recreate generated artifacts

            if ( artifact instanceof GeneratorArtifactEntity )
            {
                hi.regenerateArtifact ( (GeneratorArtifactEntity)artifact, true );
            }

            return result;
        } );
    }

    public Map<MetaKey, String> applyChannelMetaData ( final String channelId, final Map<MetaKey, String> metadata )
    {
        return this.lockManager.modifyCall ( channelId, ( ) -> {

            return doWithTransaction ( em -> {
                final StorageHandlerImpl hi = new StorageHandlerImpl ( em, this.generatorProcessor, this.lockManager );

                final ChannelEntity channel = getCheckedChannel ( em, channelId );

                testLocked ( channel );

                final Map<MetaKey, String> result = convert ( channel.getProvidedProperties () );

                // merge

                mergeMetaData ( metadata, result );

                // first clear all

                channel.getProvidedProperties ().clear ();

                em.persist ( channel );
                em.flush ();

                // now add the new set

                Helper.convertProvidedProperties ( result, channel, channel.getProvidedProperties () );

                // store

                em.persist ( channel );
                em.flush ();

                // re-run channel aggregators

                hi.runChannelAggregators ( channel );

                return result;

            } );
        } );
    }

    protected static void mergeMetaData ( final Map<MetaKey, String> metadata, final Map<MetaKey, String> result )
    {
        for ( final Map.Entry<MetaKey, String> entry : metadata.entrySet () )
        {
            if ( entry.getValue () == null )
            {
                result.remove ( entry.getKey () );
            }
            else
            {
                result.put ( entry.getKey (), entry.getValue () );
            }
        }
    }

    public SortedMap<MetaKey, String> getChannelMetaData ( final String id )
    {
        return doWithTransaction ( ( em ) -> new StorageHandlerImpl ( em, this.generatorProcessor, this.lockManager ).getChannelMetaData ( id ) );
    }

    public SortedMap<MetaKey, String> getChannelProvidedMetaData ( final String id )
    {
        return doWithTransaction ( ( em ) -> new StorageHandlerImpl ( em, this.generatorProcessor, this.lockManager ).getChannelProvidedMetaData ( id ) );
    }

    private Map<MetaKey, String> convert ( final Collection<? extends PropertyEntity> properties )
    {
        final Map<MetaKey, String> result = new HashMap<MetaKey, String> ( properties.size () );

        for ( final PropertyEntity ape : properties )
        {
            result.put ( new MetaKey ( ape.getNamespace (), ape.getKey () ), ape.getValue () );
        }

        return result;
    }

    public List<Artifact> findByName ( final String channelId, final String artifactName )
    {
        return doWithTransaction ( em -> {

            final ChannelEntity channel = getCheckedChannel ( em, channelId );

            final TypedQuery<ArtifactEntity> q = em.createQuery ( String.format ( "SELECT a FROM %s AS a WHERE a.name=:artifactName and a.channel.id=:channelId", ArtifactEntity.class.getName () ), ArtifactEntity.class );
            q.setParameter ( "artifactName", artifactName );
            q.setParameter ( "channelId", channelId );

            final ChannelImpl ci = convert ( channel );

            final List<Artifact> result = new LinkedList<> ();
            for ( final ArtifactEntity ae : q.getResultList () )
            {
                result.add ( convert ( ci, ae, null /*TODO: use properties*/) );
            }

            return result;
        } );
    }

    @Override
    public void clearChannel ( final String channelId )
    {
        this.lockManager.modifyRun ( channelId, ( ) -> {

            doWithTransactionVoid ( em -> {
                new StorageHandlerImpl ( em, this.generatorProcessor, this.lockManager ).clearChannel ( channelId );
            } );

        } );
    }

    @Override
    public void updateChannel ( final String channelId, final String name, final String description )
    {
        doWithTransactionVoid ( em -> {
            new StorageHandlerImpl ( em, this.generatorProcessor, this.lockManager ).updateChannel ( channelId, name, description );
        } );
    }

    public void generateArtifact ( final String id )
    {
        doWithTransactionVoid ( ( em ) -> {
            new StorageHandlerImpl ( em, this.generatorProcessor, this.lockManager ).generateArtifact ( id );
        } );
    }

    @Override
    public Artifact createAttachedArtifact ( final String parentArtifactId, final String name, final InputStream stream, final Map<MetaKey, String> providedMetaData )
    {
        return doWithTransaction ( ( em ) -> {
            final StorageHandlerImpl hi = new StorageHandlerImpl ( em, this.generatorProcessor, this.lockManager );
            final ArtifactEntity artifact = hi.createAttachedArtifact ( parentArtifactId, name, stream, providedMetaData );
            if ( artifact == null )
            {
                return null;
            }
            return convert ( convert ( artifact.getChannel () ), artifact, null );
        } );
    }

    public Collection<DeployKey> getAllDeployKeys ( final String channelId )
    {
        return doWithTransaction ( ( em ) -> {
            final Set<DeployKey> result = new HashSet<> ();

            final ChannelEntity channel = getCheckedChannel ( em, channelId );

            for ( final DeployGroupEntity dg : channel.getDeployGroups () )
            {
                for ( final DeployKeyEntity dk : dg.getKeys () )
                {
                    result.add ( DeployAuthServiceImpl.convert ( dk ) );
                }
            }

            return result;
        } );
    }

    public Collection<DeployGroup> getDeployGroups ( final String channelId )
    {
        return doWithTransaction ( ( em ) -> {
            final Set<DeployGroup> result = new HashSet<> ();

            final ChannelEntity channel = getCheckedChannel ( em, channelId );
            for ( final DeployGroupEntity dg : channel.getDeployGroups () )
            {
                result.add ( DeployAuthServiceImpl.convert ( dg ) );
            }

            return result;
        } );
    }

    public void addDeployGroup ( final String channelId, final String groupId )
    {
        doWithTransactionVoid ( ( em ) -> {
            final ChannelEntity channel = getCheckedChannel ( em, channelId );
            final DeployGroupEntity group = DeployAuthServiceImpl.getGroupChecked ( em, groupId );
            channel.getDeployGroups ().add ( group );
            em.persist ( channel );
        } );
    }

    public void removeDeployGroup ( final String channelId, final String groupId )
    {
        doWithTransactionVoid ( ( em ) -> {
            final ChannelEntity channel = getCheckedChannel ( em, channelId );

            final Iterator<DeployGroupEntity> i = channel.getDeployGroups ().iterator ();
            while ( i.hasNext () )
            {
                final DeployGroupEntity dg = i.next ();
                if ( dg.getId ().equals ( groupId ) )
                {
                    i.remove ();
                }
            }

            em.persist ( channel );
        } );
    }

    public Map<String, String> getChannelAspects ( final String channelId )
    {
        return doWithTransaction ( em -> {
            final ChannelEntity channel = getCheckedChannel ( em, channelId );
            return new HashMap<> ( channel.getAspects () );
        } );
    }

    public void lockChannel ( final String channelId )
    {
        setChannelLock ( channelId, true );
    }

    public void unlockChannel ( final String channelId )
    {
        setChannelLock ( channelId, false );
    }

    private void setChannelLock ( final String channelId, final boolean state )
    {
        this.lockManager.modifyRun ( channelId, ( ) -> {
            doWithTransactionVoid ( em -> {
                final ChannelEntity channel = getCheckedChannel ( em, channelId );
                channel.setLocked ( state );
                em.persist ( channel );
            } );
        } );
    }

    public void streamCacheEntry ( final String channelId, final String namespace, final String key, final ThrowingConsumer<CacheEntry> consumer )
    {
        doWithHandlerVoid ( ( handler ) -> handler.streamCacheEntry ( channelId, namespace, key, consumer ) );
    }

    protected void doWithHandlerVoid ( final ThrowingConsumer<StorageHandlerImpl> consumer )
    {
        doWithTransactionVoid ( ( em ) -> {
            final StorageHandlerImpl handler = new StorageHandlerImpl ( em, this.generatorProcessor, this.lockManager );
            consumer.accept ( handler );
        } );
    }

    protected <R> R doWithHandler ( final ManagerFunction<R, StorageHandlerImpl> consumer )
    {
        return doWithTransaction ( ( em ) -> {
            final StorageHandlerImpl handler = new StorageHandlerImpl ( em, this.generatorProcessor, this.lockManager );
            return consumer.process ( handler );
        } );
    }

    public List<CacheEntryInformation> getAllCacheEntries ( final String channelId )
    {
        return doWithHandler ( ( handler ) -> handler.getAllCacheEntries ( channelId ) );
    }

    protected <R> R doWithTransferHandler ( final ManagerFunction<R, TransferHandler> consumer )
    {
        return doWithTransaction ( ( em ) -> {
            final TransferHandler handler = new TransferHandler ( em, this.lockManager );
            return consumer.process ( handler );
        } );
    }

    protected void doWithTransferHandlerVoid ( final ThrowingConsumer<TransferHandler> consumer )
    {
        doWithTransactionVoid ( ( em ) -> {
            final TransferHandler handler = new TransferHandler ( em, this.lockManager );
            consumer.accept ( handler );
        } );
    }

    @Override
    public void exportChannel ( final String channelId, final OutputStream stream ) throws IOException
    {
        doWithTransferHandlerVoid ( ( handler ) -> handler.exportChannel ( channelId, stream ) );
    }

    @Override
    public Channel importChannel ( final InputStream inputStream, final boolean useChannelName )
    {
        return doWithTransaction ( ( em ) -> {
            final StorageHandlerImpl storage = new StorageHandlerImpl ( em, this.generatorProcessor, this.lockManager );
            final TransferHandler transfer = new TransferHandler ( em, this.lockManager );
            return convert ( transfer.importChannel ( storage, inputStream, useChannelName ) );
        } );
    }

    @Override
    public void exportAll ( final OutputStream stream ) throws IOException
    {
        doWithTransferHandlerVoid ( ( handler ) -> handler.exportAll ( stream ) );
    }

    @Override
    public void importAll ( final InputStream inputStream, final boolean useChannelNames, final boolean wipe )
    {
        doWithTransactionVoid ( ( em ) -> {
            final StorageHandlerImpl storage = new StorageHandlerImpl ( em, this.generatorProcessor, this.lockManager );
            final TransferHandler transfer = new TransferHandler ( em, this.lockManager );
            transfer.importAll ( storage, inputStream, useChannelNames, wipe );
        } );
    }

    @Override
    public void wipeClean ()
    {
        doWithHandlerVoid ( ( storage ) -> storage.wipeAllChannels () );
    }
}
