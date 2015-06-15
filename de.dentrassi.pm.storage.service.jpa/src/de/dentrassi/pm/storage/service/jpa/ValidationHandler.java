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

import static de.dentrassi.pm.storage.service.jpa.Helper.convert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dentrassi.osgi.profiler.Profile;
import de.dentrassi.osgi.profiler.Profile.Handle;
import de.dentrassi.pm.common.Severity;
import de.dentrassi.pm.storage.ValidationMessage;
import de.dentrassi.pm.storage.jpa.AggregatorValidationMessageEntity;
import de.dentrassi.pm.storage.jpa.ArtifactEntity;
import de.dentrassi.pm.storage.jpa.ChannelEntity;
import de.dentrassi.pm.storage.jpa.ValidationMessageEntity;
import de.dentrassi.pm.storage.jpa.ValidationSeverity;

public class ValidationHandler extends AbstractHandler
{
    @FunctionalInterface
    private interface Target
    {
        public void apply ( ArtifactEntity entity, long value );
    }

    private final static Logger logger = LoggerFactory.getLogger ( ValidationHandler.class );

    public ValidationHandler ( final EntityManager em )
    {
        super ( em );
    }

    public void aggregateChannel ( final ChannelEntity channel )
    {
        try ( Handle handle = Profile.start ( this, "aggregateChannel" ) )
        {
            logger.debug ( "Aggregating channel information: {}", channel.getId () );

            final Query q = this.em.createQuery ( String.format ( "SELECT vm.severity, count(vm) from %s vm where vm.channel=:CHANNEL group by vm.severity", ValidationMessageEntity.class.getName () ) );
            q.setParameter ( "CHANNEL", channel );

            channel.setAggregatedNumberOfErrors ( 0 );
            channel.setAggregatedNumberOfWarnings ( 0 );

            for ( final Object row : q.getResultList () )
            {
                final Object[] fields = (Object[])row;

                if ( fields[0] == ValidationSeverity.ERROR )
                {
                    channel.setAggregatedNumberOfErrors ( ( (Number)fields[1] ).longValue () );
                }
                else if ( fields[0] == ValidationSeverity.WARNING )
                {
                    channel.setAggregatedNumberOfWarnings ( ( (Number)fields[1] ).longValue () );
                }
            }

            this.em.persist ( channel );
            this.em.flush ();
        }
    }

    @Deprecated
    public void aggregateArtifact ( final String artifactId )
    {
        logger.debug ( "Aggregating artifact information by id: {}", artifactId );
        aggregateArtifact ( this.em.getReference ( ArtifactEntity.class, artifactId ) );
    }

    @Deprecated
    public void aggregateArtifact ( final ArtifactEntity artifact )
    {
        try ( Handle handle = Profile.start ( this, "aggregateArtifact" ) )
        {
            logger.debug ( "Aggregating artifact information: {}", artifact.getId () );

            final Query q = this.em.createQuery ( String.format ( "SELECT vm.severity, count(vm) from %s vm where :ARTIFACT MEMBER OF vm.artifacts group by vm.severity", ValidationMessageEntity.class.getName () ) );
            q.setParameter ( "ARTIFACT", artifact );

            artifact.setAggregatedNumberOfErrors ( 0 );
            artifact.setAggregatedNumberOfWarnings ( 0 );

            for ( final Object row : q.getResultList () )
            {
                final Object[] fields = (Object[])row;

                if ( fields[0] == ValidationSeverity.ERROR )
                {
                    artifact.setAggregatedNumberOfErrors ( ( (Number)fields[1] ).longValue () );
                }
                else if ( fields[0] == ValidationSeverity.WARNING )
                {
                    artifact.setAggregatedNumberOfWarnings ( ( (Number)fields[1] ).longValue () );
                }
            }

            this.em.persist ( artifact );
            this.em.flush ();
        }
    }

    public void aggregateArtifacts ( final ChannelEntity channel, final Collection<String> artifactIds )
    {
        // first set all to zero

        {
            final Query q = this.em.createQuery ( String.format ( "UPDATE %s ae SET ae.aggregatedNumberOfWarnings=0, ae.aggregatedNumberOfErrors=0 WHERE ae.id in :IDS", ArtifactEntity.class.getName () ) );
            q.setParameter ( "IDS", artifactIds );
            q.executeUpdate ();
        }

        aggregateSeverity ( channel, Severity.ERROR, ArtifactEntity::setAggregatedNumberOfErrors );
        aggregateSeverity ( channel, Severity.WARNING, ArtifactEntity::setAggregatedNumberOfWarnings );

        this.em.flush ();
    }

    private void aggregateSeverity ( final ChannelEntity channel, final Severity severity, final BiConsumer<ArtifactEntity, Long> consumer )
    {
        final Map<ArtifactEntity, Long> aggrMap = getChannelMap ( channel, severity );

        for ( final Map.Entry<ArtifactEntity, Long> entry : aggrMap.entrySet () )
        {
            final Long count = entry.getValue ();
            if ( count > 0 )
            {
                consumer.accept ( entry.getKey (), count );
                this.em.persist ( entry.getKey () );
            }
        }
    }

    private Map<ArtifactEntity, Long> getChannelMap ( final ChannelEntity channel, final Severity severity )
    {
        final Query q = this.em.createQuery ( "SELECT ae, COUNT(vm.severity) FROM %s ae LEFT OUTER JOIN ae.messages. " );
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Aggregate the channel and all artifacts
     *
     * @param channel
     *            the channel to process
     */
    public void aggregateFullChannel ( final ChannelEntity channel )
    {
        try ( Handle handle = Profile.start ( this, "aggregateFullChannel" ) )
        {
            // reset all artifacts initially ... we only process selected artifacts later on

            {
                final Query q = this.em.createQuery ( String.format ( "UPDATE %s ae SET ae.aggregatedNumberOfWarnings=0, ae.aggregatedNumberOfErrors=0 WHERE ae.channel=:CHANNEL", ArtifactEntity.class.getName () ) );
                q.setParameter ( "CHANNEL", channel );
                q.executeUpdate ();
            }

            // start

            long channelWarnings = 0;
            long channelErrors = 0;

            // query

            final TypedQuery<ValidationMessageEntity> q = this.em.createQuery ( String.format ( "SELECT vm from %s vm where vm.channel=:CHANNEL", ValidationMessageEntity.class.getName () ), ValidationMessageEntity.class );
            q.setParameter ( "CHANNEL", channel );

            // process

            final Map<ArtifactEntity, Long> warningArts = new HashMap<> ();
            final Map<ArtifactEntity, Long> errorArts = new HashMap<> ();

            for ( final ValidationMessageEntity vme : q.getResultList () )
            {
                final Map<ArtifactEntity, Long> map;
                switch ( vme.getSeverity () )
                {
                    case WARNING:
                        map = warningArts;
                        channelWarnings++;
                        break;
                    case ERROR:
                        map = errorArts;
                        channelErrors++;
                        break;
                    case INFO: //$FALL-THROUGH$
                    default:
                        map = null;
                        break;
                }

                if ( map == null )
                {
                    continue;
                }

                increment ( map, vme.getArtifacts () );
            }

            // write out information

            channel.setAggregatedNumberOfWarnings ( channelWarnings );
            channel.setAggregatedNumberOfErrors ( channelErrors );

            writeSeverity ( warningArts, ArtifactEntity::setAggregatedNumberOfWarnings );
            writeSeverity ( errorArts, ArtifactEntity::setAggregatedNumberOfErrors );

            this.em.flush ();
        }
    }

    /**
     * Increment the counter in the map for all artifacts
     */
    private static void increment ( final Map<ArtifactEntity, Long> map, final Set<ArtifactEntity> artifacts )
    {
        for ( final ArtifactEntity ae : artifacts )
        {
            final Long value = map.get ( ae );
            if ( value == null )
            {
                map.put ( ae, 1L );
            }
            else
            {
                map.put ( ae, value + 1L );
            }
        }
    }

    private void writeSeverity ( final Map<ArtifactEntity, Long> map, final Target target )
    {
        for ( final Map.Entry<ArtifactEntity, Long> entry : map.entrySet () )
        {
            // we set this to zero and back again to force an update
            // the previous UPDATE query is not seen by the entity manager
            target.apply ( entry.getKey (), 0L );
            target.apply ( entry.getKey (), entry.getValue () );
            this.em.persist ( entry.getKey () );
        }
    }

    public List<ValidationMessage> getValidationMessagesForArtifact ( final String artifactId )
    {
        final ArtifactEntity artifact = this.em.getReference ( ArtifactEntity.class, artifactId );

        final TypedQuery<ValidationMessageEntity> q = this.em.createQuery ( String.format ( "SELECT vm from %s vm where :ARTIFACT MEMBER OF vm.artifacts", ValidationMessageEntity.class.getName () ), ValidationMessageEntity.class );
        q.setParameter ( "ARTIFACT", artifact );

        return convertMessages ( q.getResultList () );
    }

    public List<ValidationMessage> getValidationMessages ( final String channelId )
    {
        final TypedQuery<ValidationMessageEntity> q = this.em.createQuery ( String.format ( "SELECT vm from %s vm where vm.channel.id=:CHANNEL_ID", ValidationMessageEntity.class.getName () ), ValidationMessageEntity.class );
        q.setParameter ( "CHANNEL_ID", channelId );

        return convertMessages ( q.getResultList () );
    }

    private static List<ValidationMessage> convertMessages ( final List<ValidationMessageEntity> resultList )
    {
        if ( resultList == null )
        {
            return null;
        }

        final List<ValidationMessage> result = new ArrayList<> ( resultList.size () );

        for ( final ValidationMessageEntity vme : resultList )
        {
            result.add ( convertMessage ( vme ) );
        }

        return result;
    }

    private static ValidationMessage convertMessage ( final ValidationMessageEntity vme )
    {
        if ( vme == null )
        {
            return null;
        }

        return new ValidationMessage ( convert ( vme.getSeverity () ), vme.getMessage (), vme.getNamespace (), vme.getArtifacts ().stream ().map ( ArtifactEntity::getId ).collect ( Collectors.toCollection ( TreeSet::new ) ) );
    }

    public void createMessage ( final ChannelEntity channel, final String namespace, final Severity severity, final String message, final Set<String> artifactIds, final Supplier<ValidationMessageEntity> supplier )
    {
        try ( Handle handle = Profile.start ( this, "createMessage" ) )
        {
            final ValidationMessageEntity vme = supplier.get ();

            vme.setChannel ( channel );
            vme.setNamespace ( namespace );

            vme.setSeverity ( Helper.convert ( severity ) );
            vme.setMessage ( message );

            vme.setArtifacts ( artifactIds.stream ().map ( id -> this.em.getReference ( ArtifactEntity.class, id ) ).collect ( Collectors.toSet () ) );

            this.em.persist ( vme );
        }
    }

    public void deleteAllAggregatorMessages ( final ChannelEntity channel )
    {
        try ( Handle handle = Profile.start ( this, "deleteAllAggregatorMessages" ) )
        {
            final Query q = this.em.createQuery ( String.format ( "DELETE from %s vme where vme.channel=:CHANNEL", AggregatorValidationMessageEntity.class.getName () ) );
            q.setParameter ( "CHANNEL", channel );
            q.executeUpdate ();
        }
    }

}
