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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;

import org.osgi.framework.FrameworkUtil;

import de.dentrassi.pm.common.ArtifactInformation;
import de.dentrassi.pm.common.ChannelAspectInformation;
import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.common.service.AbstractJpaServiceImpl;
import de.dentrassi.pm.generator.GeneratorProcessor;
import de.dentrassi.pm.storage.Artifact;
import de.dentrassi.pm.storage.ArtifactReceiver;
import de.dentrassi.pm.storage.Channel;
import de.dentrassi.pm.storage.jpa.ArtifactEntity;
import de.dentrassi.pm.storage.jpa.ArtifactPropertyEntity;
import de.dentrassi.pm.storage.jpa.ChannelEntity;
import de.dentrassi.pm.storage.jpa.DerivedArtifactEntity;
import de.dentrassi.pm.storage.jpa.ExtractedArtifactPropertyEntity;
import de.dentrassi.pm.storage.jpa.GeneratorArtifactEntity;
import de.dentrassi.pm.storage.jpa.ProvidedArtifactPropertyEntity;
import de.dentrassi.pm.storage.jpa.StoredArtifactEntity;
import de.dentrassi.pm.storage.jpa.VirtualArtifactEntity;
import de.dentrassi.pm.storage.service.StorageService;

public class StorageServiceImpl extends AbstractJpaServiceImpl implements StorageService, StreamServiceHelper
{

    private final GeneratorProcessor generatorProcessor = new GeneratorProcessor ( FrameworkUtil.getBundle ( StorageServiceImpl.class ).getBundleContext () );

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
        }, stream, providedMetaData, true );
    }

    @Override
    public Artifact createArtifact ( final String channelId, final String name, final InputStream stream, final Map<MetaKey, String> providedMetaData )
    {
        return internalCreateArtifact ( channelId, name, StoredArtifactEntity::new, stream, providedMetaData, true );
    }

    protected Artifact internalCreateArtifact ( final String channelId, final String name, final Supplier<ArtifactEntity> entityCreator, final InputStream stream, final Map<MetaKey, String> providedMetaData, final boolean runChannelListeners )
    {
        final Artifact artifact;
        try
        {
            artifact = doWithTransaction ( em -> {
                final ChannelEntity channel = getCheckedChannel ( em, channelId );
                final StorageHandlerImpl hi = new StorageHandlerImpl ( em, this.generatorProcessor );
                final ArtifactEntity ae = hi.performStoreArtifact ( channel, name, stream, em, entityCreator, providedMetaData );
                return convert ( convert ( ae.getChannel () ), ae );
            } );
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

        return artifact;
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

            final StorageHandlerImpl hi = new StorageHandlerImpl ( em, this.generatorProcessor );
            final ChannelEntity ce = hi.getCheckedChannel ( channelId );
            final ChannelImpl channel = convert ( ce );
            return hi.<Artifact> listArtifacts ( channelId, ( ae ) -> convert ( channel, ae ) );
        } );
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

        final Map<MetaKey, String> metadata = convertMetaData ( ae );

        if ( ae instanceof GeneratorArtifactEntity )
        {
            return new GeneratorArtifactImpl ( channel, ae.getId (), ae.getName (), ae.getSize (), metadata, ae.getCreationTimestamp () );
        }
        else
        {
            return new ArtifactImpl ( channel, ae.getId (), ae.getName (), ae.getSize (), metadata, ae.getCreationTimestamp (), ae instanceof DerivedArtifactEntity, false );
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
    public ArtifactInformation deleteArtifact ( final String artifactId )
    {
        return doWithTransaction ( em -> {
            return new StorageHandlerImpl ( em, this.generatorProcessor ).deleteArtifact ( artifactId );
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

            new StorageHandlerImpl ( em, this.generatorProcessor ).reprocessAspect ( channel, aspectFactoryId );
        } );
    }

    @Override
    public void removeChannelAspect ( final String channelId, final String aspectFactoryId )
    {
        doWithTransactionVoid ( em -> {
            final ChannelEntity channel = getCheckedChannel ( em, channelId );
            channel.getAspects ().remove ( aspectFactoryId );
            em.persist ( channel );

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
                final Query q = em.createQuery ( String.format ( "DELETE from %s va where va.namespace=:factoryId and va.channel.id=:channelId", VirtualArtifactEntity.class.getSimpleName () ) );
                q.setParameter ( "factoryId", aspectFactoryId );
                q.setParameter ( "channelId", channelId );
                q.executeUpdate ();
            }

        } );
    }

    @Override
    public ArtifactInformation getArtifactInformation ( final String artifactId )
    {
        return doWithTransaction ( em -> convert ( getCheckedArtifact ( em, artifactId ) ) );
    }

    @Override
    public Artifact getArtifact ( final String artifactId )
    {
        return doWithTransaction ( em -> {
            final ArtifactEntity artifact = getCheckedArtifact ( em, artifactId );
            final ChannelImpl channel = convert ( artifact.getChannel () );
            return convert ( channel, artifact );
        } );
    }

    public Map<MetaKey, String> applyMetaData ( final String artifactId, final Map<MetaKey, String> metadata )
    {
        return doWithTransaction ( em -> {
            final StorageHandlerImpl hi = new StorageHandlerImpl ( em, this.generatorProcessor );

            final ArtifactEntity artifact = getCheckedArtifact ( em, artifactId );
            final Map<MetaKey, String> result = convert ( artifact.getProvidedProperties () );

            // apply
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

            // first clear all
            artifact.getProvidedProperties ().clear ();
            em.persist ( artifact );
            em.flush ();

            // now add the new set
            Helper.convertProvidedProperties ( result, artifact, artifact.getProvidedProperties () );

            // store
            em.persist ( artifact );
            em.flush ();

            if ( artifact instanceof GeneratorArtifactEntity )
            {
                hi.regenerateArtifact ( (GeneratorArtifactEntity)artifact );
            }

            return result;
        } );
    }

    private Map<MetaKey, String> convert ( final Collection<? extends ArtifactPropertyEntity> properties )
    {
        final Map<MetaKey, String> result = new HashMap<MetaKey, String> ( properties.size () );

        for ( final ArtifactPropertyEntity ape : properties )
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
            final Query q = em.createQuery ( String.format ( "DELETE from %s ae where ae.channel.id=:channelId", ArtifactEntity.class.getName () ) );
            q.setParameter ( "channelId", channelId );
            q.executeUpdate ();
        } );
    }

    @Override
    public void updateChannel ( final String channelId, final String name )
    {
        doWithTransactionVoid ( em -> {
            new StorageHandlerImpl ( em, this.generatorProcessor ).updateChannel ( channelId, name );
        } );
    }

    public void generateArtifact ( final String id )
    {
        doWithTransactionVoid ( ( em ) -> {
            new StorageHandlerImpl ( em, this.generatorProcessor ).generateArtifact ( id );
        } );
    }

}
