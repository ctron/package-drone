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
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dentrassi.pm.storage.ValidationMessage;
import de.dentrassi.pm.storage.jpa.ArtifactEntity;
import de.dentrassi.pm.storage.jpa.ChannelEntity;
import de.dentrassi.pm.storage.jpa.ValidationMessageEntity;
import de.dentrassi.pm.storage.jpa.ValidationSeverity;

public class ValidationHandler extends AbstractHandler
{
    private final static Logger logger = LoggerFactory.getLogger ( ValidationHandler.class );

    public ValidationHandler ( final EntityManager em, final LockManager<String> lockManager )
    {
        super ( em, lockManager );
    }

    public void agregateChannel ( final ChannelEntity channel )
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
    }

    public void agregateArtifact ( final ArtifactEntity artifact )
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

}
