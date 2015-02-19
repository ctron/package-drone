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
package de.dentrassi.pm.storage.service.jpa;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
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

import de.dentrassi.osgi.web.LinkTarget;
import de.dentrassi.pm.common.ArtifactInformation;
import de.dentrassi.pm.common.ChannelAspectInformation;
import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.common.SimpleArtifactInformation;
import de.dentrassi.pm.common.service.AbstractJpaServiceImpl;
import de.dentrassi.pm.generator.GeneratorProcessor;
import de.dentrassi.pm.storage.Artifact;
import de.dentrassi.pm.storage.ArtifactReceiver;
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

    private final LockManager<ChannelEntity, String> lockManager;

    public StorageServiceImpl ()
    {
        this.lockManager = new LockManager<> ( ChannelEntity::getId );
    }

    public void start ()
    {
        this.generatorProcessor.open ();
    }

    public void stop ()
    {
        this.generatorProcessor.close ();
    }

    @Override
    public Channel createChannel ()
    {
        final ChannelEntity channel = new ChannelEntity ();
        return doWithTransaction ( em -> {
            em.persist ( channel );
            return convert ( channel );
        } );
    }

    @Override
    public void deleteChannel ( final String channelId )
    {
        doWithTransactionVoid ( em -> em.remove ( em.getReference ( ChannelEntity.class, channelId ) ) );
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
        return doWithTransaction ( ( em ) -> {
            final StorageHandlerImpl hi = new StorageHandlerImpl ( em, this.generatorProcessor, this.lockManager );
            final ArtifactEntity artifact = hi.internalCreateArtifact ( channelId, name, entityCreator, stream, providedMetaData, true );

            if ( artifact == null )
            {
                return null;
            }

            return convert ( convert ( artifact.getChannel () ), artifact );
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
            return hi.<Artifact> listArtifacts ( channelId, ( ae ) -> convert ( channel, ae ) );
        } );
    }

    public Set<SimpleArtifactInformation> listSimpleArtifacts ( final String channelId )
    {
        return doWithTransaction ( em -> {

            final StorageHandlerImpl hi = new StorageHandlerImpl ( em, this.generatorProcessor, this.lockManager );
            return hi.<SimpleArtifactInformation> listArtifacts ( channelId, ( ae ) -> convertSimple ( ae ) );
        } );
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

    private ChannelImpl convert ( final ChannelEntity ce )
    {
        if ( ce == null )
        {
            return null;
        }
        return new ChannelImpl ( ce.getId (), ce.getName (), this );
    }

    private Artifact convert ( final ChannelImpl channel, final ArtifactEntity ae )
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
            return new GeneratorArtifactImpl ( channel, ae.getId (), convert ( ae ), targets[0] );
        }
        else
        {
            return new ArtifactImpl ( channel, ae.getId (), convert ( ae ) );
        }
    }

    @Override
    public void streamArtifact ( final String artifactId, final ArtifactReceiver receiver )
    {
        doWithTransactionVoid ( em -> {
            final ArtifactEntity ae = getCheckedArtifact ( em, artifactId );
            internalStreamArtifact ( em, ae, receiver );
        } );
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
            return Activator.getChannelAspects ().resolve ( channel.getAspects () );
        } );
    }

    @Override
    public void addChannelAspect ( final String channelId, final String aspectFactoryId )
    {
        doWithTransactionVoid ( em -> {

            final ChannelEntity channel = getCheckedChannel ( em, channelId );
            channel.getAspects ().add ( aspectFactoryId );
            em.persist ( channel );
            em.flush ();

            new StorageHandlerImpl ( em, this.generatorProcessor, this.lockManager ).reprocessAspect ( channel, aspectFactoryId );
        } );
    }

    @Override
    public void refreshChannelAspect ( final String channelId, final String aspectFactoryId )
    {
        doWithTransactionVoid ( em -> {

            final ChannelEntity channel = getCheckedChannel ( em, channelId );

            if ( channel.getAspects ().contains ( aspectFactoryId ) )
            {
                new StorageHandlerImpl ( em, this.generatorProcessor, this.lockManager ).reprocessAspect ( channel, aspectFactoryId );
            }
        } );
    }

    @Override
    public void removeChannelAspect ( final String channelId, final String aspectFactoryId )
    {
        doWithTransactionVoid ( em -> {
            final ChannelEntity channel = getCheckedChannel ( em, channelId );

            this.lockManager.writeLock ( channel );
            try
            {

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
            }
            finally
            {
                this.lockManager.writeUnlock ( channel );
            }
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
        return doWithTransaction ( em -> convert ( getArtifact ( em, artifactId ) ) );
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
            return convert ( convert ( artifact.getChannel () ), artifact );
        } );
    }

    public Map<MetaKey, String> applyMetaData ( final String artifactId, final Map<MetaKey, String> metadata )
    {
        return doWithTransaction ( em -> {
            final StorageHandlerImpl hi = new StorageHandlerImpl ( em, this.generatorProcessor, this.lockManager );

            final ArtifactEntity artifact = getCheckedArtifact ( em, artifactId );
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
        return doWithTransaction ( em -> {
            final StorageHandlerImpl hi = new StorageHandlerImpl ( em, this.generatorProcessor, this.lockManager );

            final ChannelEntity channel = getCheckedChannel ( em, channelId );

            this.lockManager.writeLock ( channel );
            try
            {
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
            }
            finally
            {
                this.lockManager.writeUnlock ( channel );
            }
        } );
    }

    protected void mergeMetaData ( final Map<MetaKey, String> metadata, final Map<MetaKey, String> result )
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

    public Collection<Artifact> findByName ( final String channelId, final String artifactName )
    {
        return doWithTransaction ( em -> {

            final ChannelEntity channel = getCheckedChannel ( em, channelId );

            final TypedQuery<ArtifactEntity> q = em.createQuery ( String.format ( "SELECT a FROM %s AS a WHERE a.name=:artifactName and a.channel.id=:channelId", ArtifactEntity.class.getName () ), ArtifactEntity.class );
            q.setParameter ( "artifactName", artifactName );
            q.setParameter ( "channelId", channelId );

            final ChannelImpl ci = convert ( channel );

            final Collection<Artifact> result = new LinkedList<> ();
            for ( final ArtifactEntity ae : q.getResultList () )
            {
                result.add ( convert ( ci, ae ) );
            }

            return result;
        } );
    }

    @Override
    public void clearChannel ( final String channelId )
    {
        doWithTransactionVoid ( em -> {
            final ChannelEntity channel = getCheckedChannel ( em, channelId );

            this.lockManager.writeLock ( channel );
            try
            {
                final Query q = em.createQuery ( String.format ( "DELETE from %s ae where ae.channel.id=:channelId", ArtifactEntity.class.getName () ) );
                q.setParameter ( "channelId", channelId );
                q.executeUpdate ();
                new StorageHandlerImpl ( em, this.generatorProcessor, this.lockManager ).runChannelAggregators ( channelId );
            }
            finally
            {
                this.lockManager.writeUnlock ( channel );
            }
        } );
    }

    @Override
    public void updateChannel ( final String channelId, final String name )
    {
        doWithTransactionVoid ( em -> {
            new StorageHandlerImpl ( em, this.generatorProcessor, this.lockManager ).updateChannel ( channelId, name );
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
            final ArtifactEntity artifact = new StorageHandlerImpl ( em, this.generatorProcessor, this.lockManager ).createAttachedArtifact ( parentArtifactId, name, stream, providedMetaData );
            if ( artifact == null )
            {
                return null;
            }
            return convert ( convert ( artifact.getChannel () ), artifact );
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

    public Set<String> getChannelAspects ( final String channelId )
    {
        return doWithTransaction ( ( em ) -> {
            final ChannelEntity channel = getCheckedChannel ( em, channelId );
            return new HashSet<> ( channel.getAspects () );
        } );
    }
}
