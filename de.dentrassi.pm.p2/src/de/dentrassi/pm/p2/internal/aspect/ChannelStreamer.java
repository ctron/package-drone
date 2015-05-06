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
package de.dentrassi.pm.p2.internal.aspect;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import de.dentrassi.pm.common.ArtifactInformation;
import de.dentrassi.pm.common.MetaKey;

public class ChannelStreamer
{
    private final LinkedList<Processor> processors;

    private ChecksumValidatorProcessor validator;

    public ChannelStreamer ( final String channelNameOrId, final Map<MetaKey, String> channelMetaData, final boolean writeCompressed, final boolean writePlain )
    {
        final String title = makeTitle ( channelNameOrId, null, channelMetaData );

        this.processors = new LinkedList<> ();
        if ( writeCompressed )
        {
            this.processors.add ( new MetaDataProcessor ( title, true ) );
            this.processors.add ( new ArtifactsProcessor ( title, true ) );
        }
        if ( writePlain )
        {
            this.processors.add ( new MetaDataProcessor ( title, false ) );
            this.processors.add ( new ArtifactsProcessor ( title, false ) );
        }
        this.processors.add ( this.validator = new ChecksumValidatorProcessor () );
    }

    public static String makeTitle ( final String id, final String name, final Map<MetaKey, String> channelMetaData )
    {
        final String title = channelMetaData.get ( new MetaKey ( "p2.repo", "title" ) );
        if ( title != null && !title.isEmpty () )
        {
            return title;
        }

        final String channelName = name != null ? name : id;

        return String.format ( "Package Drone Channel: %s", channelName );
    }

    public void stream ( final Collection<ArtifactInformation> artifacts, final ArtifactStreamer streamer )
    {
        for ( final ArtifactInformation artifact : artifacts )
        {
            process ( artifact, streamer );
        }
    }

    public void process ( final ArtifactInformation artifact, final ArtifactStreamer streamer )
    {
        for ( final Processor processor : this.processors )
        {
            try
            {
                processor.process ( artifact, streamer );
            }
            catch ( final Exception e )
            {
                throw new RuntimeException ( e );
            }
        }
    }

    public void spoolOut ( final SpoolOutHandler handler ) throws IOException
    {
        for ( final Processor processor : this.processors )
        {
            if ( processor.getId () != null )
            {
                handler.spoolOut ( processor.getId (), processor.getName (), processor.getMimeType (), ( stream ) -> processor.write ( stream ) );
            }
        }
    }

    public Map<String, Set<String>> checkDuplicates ()
    {
        return this.validator.checkDuplicates ();
    }

}
