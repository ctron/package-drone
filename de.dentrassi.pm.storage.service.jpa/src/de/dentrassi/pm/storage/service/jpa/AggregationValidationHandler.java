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

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dentrassi.pm.common.Severity;
import de.dentrassi.pm.storage.jpa.AggregatorValidationMessageEntity;
import de.dentrassi.pm.storage.jpa.ChannelEntity;

public class AggregationValidationHandler
{
    private final static Logger logger = LoggerFactory.getLogger ( AggregationValidationHandler.class );

    private final ValidationHandler handler;

    private final Set<String> affectedArtifactIds = new HashSet<> ();

    private final Set<ChannelEntity> affectedChannels = new HashSet<> ();

    public AggregationValidationHandler ( final ValidationHandler handler )
    {
        this.handler = handler;
    }

    public void createMessage ( final ChannelEntity channel, final String namespace, final Severity severity, final String message, final Set<String> artifactIds )
    {
        this.handler.createMessage ( channel, namespace, severity, message, artifactIds, AggregatorValidationMessageEntity::new );

        this.affectedChannels.add ( channel );
        this.affectedArtifactIds.addAll ( artifactIds );
    }

    public void flush ()
    {
        logger.debug ( "Flushing validation messages" );

        for ( final ChannelEntity channel : this.affectedChannels )
        {
            this.handler.aggregateChannel ( channel );
        }

        for ( final String artifactId : this.affectedArtifactIds )
        {
            this.handler.aggregateArtifact ( artifactId );
        }

        /*
        this.affectedChannels.clear ();
        this.affectedArtifactIds.clear ();
        */
    }
}
