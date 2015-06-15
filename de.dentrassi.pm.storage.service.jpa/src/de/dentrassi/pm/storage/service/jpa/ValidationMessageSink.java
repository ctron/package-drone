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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dentrassi.osgi.profiler.Profile;
import de.dentrassi.osgi.profiler.Profile.Handle;
import de.dentrassi.pm.common.Severity;
import de.dentrassi.pm.storage.jpa.ArtifactEntity;
import de.dentrassi.pm.storage.jpa.ChannelEntity;
import de.dentrassi.pm.storage.jpa.ExtractorValidationMessageEntity;
import de.dentrassi.pm.storage.jpa.ValidationMessageEntity;
import de.dentrassi.pm.storage.jpa.ValidationSeverity;

public class ValidationMessageSink
{
    private final static Logger logger = LoggerFactory.getLogger ( ValidationMessageSink.class );

    private static class Entry
    {
        private final String aspectId;

        private final ValidationSeverity severity;

        private final String message;

        public Entry ( final String aspectId, final ValidationSeverity severity, final String message )
        {
            this.aspectId = aspectId;
            this.severity = severity;
            this.message = message;
        }

        public String getAspectId ()
        {
            return this.aspectId;
        }

        public String getMessage ()
        {
            return this.message;
        }

        public ValidationSeverity getSeverity ()
        {
            return this.severity;
        }
    }

    private final ChannelEntity channel;

    private final List<Entry> entries = new LinkedList<> ();

    private final ValidationHandler handler;

    public ValidationMessageSink ( final ChannelEntity channel, final ValidationHandler handler )
    {
        this.channel = channel;
        this.handler = handler;
    }

    public void addMessage ( final String aspectId, final Severity severity, final String message )
    {
        this.entries.add ( new Entry ( aspectId, convert ( severity ), message ) );
    }

    public void flush ( final EntityManager em, final ArtifactEntity artifact )
    {
        try ( Handle handle = Profile.start ( this, "flush" ) )
        {
            if ( logger.isDebugEnabled () )
            {
                logger.debug ( "Flushing validation messages - artifact: {}", artifact.getId () );
            }

            for ( final Entry entry : this.entries )
            {
                final ValidationMessageEntity vme = new ExtractorValidationMessageEntity ();
                vme.setChannel ( this.channel );
                vme.setSeverity ( entry.getSeverity () );
                vme.setMessage ( entry.getMessage () );
                vme.setArtifacts ( Collections.singleton ( artifact ) );
                vme.setNamespace ( entry.getAspectId () );

                em.persist ( vme );
            }
            this.entries.clear ();

            this.handler.aggregateArtifact ( artifact );
        }
    }
}
