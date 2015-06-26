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

import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import de.dentrassi.pm.common.Severity;
import de.dentrassi.pm.storage.jpa.AggregatorValidationMessageEntity;
import de.dentrassi.pm.storage.jpa.ChannelEntity;

public class AggregationValidationHandler
{
    private final ValidationHandler handler;

    private final Multimap<ChannelEntity, String> affectedMap = HashMultimap.create ();

    public AggregationValidationHandler ( final ValidationHandler handler )
    {
        this.handler = handler;
    }

    public void createMessage ( final ChannelEntity channel, final String namespace, final Severity severity, final String message, final Set<String> artifactIds )
    {
        this.handler.createMessage ( channel, namespace, severity, message, artifactIds, AggregatorValidationMessageEntity::new );

        this.affectedMap.putAll ( channel, artifactIds );
    }
}
