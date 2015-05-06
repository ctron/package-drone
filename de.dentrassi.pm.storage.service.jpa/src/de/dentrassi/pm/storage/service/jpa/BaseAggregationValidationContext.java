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

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.dentrassi.pm.aspect.aggregate.AggregationValidationContext;
import de.dentrassi.pm.common.Severity;
import de.dentrassi.pm.storage.jpa.ChannelEntity;

/**
 * Provides a base implementation for {@link AggregationValidationContext}
 * <p>
 * This implementation will pick up validation messages and persist them when
 * the flush method is called.
 * </p>
 * <p>
 * The context can only be used for one namespace at a time.
 * </p>
 */
public class BaseAggregationValidationContext
{
    private static final class Entry
    {
        private final Severity severity;

        private final String message;

        private final Set<String> artifacts;

        public Entry ( final Severity severity, final String message, final Set<String> artifacts )
        {
            this.severity = severity;
            this.message = message;
            this.artifacts = artifacts;
        }

        public Severity getSeverity ()
        {
            return this.severity;
        }

        public String getMessage ()
        {
            return this.message;
        }

        public Set<String> getArtifacts ()
        {
            return this.artifacts;
        }
    }

    private final ChannelEntity channel;

    private final String namespace;

    private final List<Entry> entries = new LinkedList<> ();

    public BaseAggregationValidationContext ( final ChannelEntity channel, final String namespace )
    {
        this.channel = channel;
        this.namespace = namespace;
    }

    /**
     * Receive a validation message
     *
     * @param severity
     *            the severity
     * @param message
     *            the message
     * @param artifactIds
     *            the references artifacts
     */
    public void validationMessage ( final Severity severity, final String message, final Set<String> artifactIds )
    {
        this.entries.add ( new Entry ( severity, message, artifactIds ) );
    }

    /**
     * This method writes out the validation messages, but <em>does not</em>
     * aggregate the channel or the artifacts
     *
     * @param handler
     *            the handler to use for processing
     */
    public void flush ( final AggregationValidationHandler handler )
    {
        // write messages

        for ( final Entry entry : this.entries )
        {
            handler.createMessage ( this.channel, this.namespace, entry.getSeverity (), entry.getMessage (), entry.getArtifacts () );
        }

        // clear internal state

        this.entries.clear ();
    }
}
