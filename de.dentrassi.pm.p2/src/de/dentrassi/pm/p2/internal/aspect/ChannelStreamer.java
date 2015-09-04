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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.xpath.XPathFactory;

import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.common.XmlHelper;
import de.dentrassi.pm.storage.channel.ArtifactInformation;

public class ChannelStreamer
{
    public static final MetaKey MK_FRAGMENT_TYPE = new MetaKey ( "p2.repo", "fragment-type" );

    private final LinkedList<Processor> processors;

    private ChecksumValidatorProcessor validator;

    private final Map<String, Object> context = new HashMap<> ();

    private final XmlHelper xml = new XmlHelper ();

    public ChannelStreamer ( final String channelId, final Map<MetaKey, String> channelMetaData, final boolean writeCompressed, final boolean writePlain )
    {
        final String title = makeTitle ( channelId, channelMetaData );

        this.processors = new LinkedList<> ();

        try
        {
            final DocumentBuilder documentBuilder = this.xml.getBuilder ();
            final XPathFactory pathFactory = XmlHelper.createXPathFactory ();

            final DocumentCache cache = new DocumentCache ( documentBuilder );

            this.processors.add ( this.validator = new ChecksumValidatorProcessor ( cache, pathFactory ) );

            if ( writeCompressed )
            {
                this.processors.add ( new MetaDataProcessor ( title, true, cache, documentBuilder, pathFactory ) );
                this.processors.add ( new ArtifactsProcessor ( title, true, cache, pathFactory ) );
            }

            if ( writePlain )
            {
                this.processors.add ( new MetaDataProcessor ( title, false, cache, documentBuilder, pathFactory ) );
                this.processors.add ( new ArtifactsProcessor ( title, false, cache, pathFactory ) );
            }

        }
        catch ( final Exception e )
        {
            throw new RuntimeException ( e );
        }

    }

    public static String makeTitle ( final String id, final Map<MetaKey, String> channelMetaData )
    {
        final String title = channelMetaData.get ( new MetaKey ( "p2.repo", "title" ) );
        if ( title != null && !title.isEmpty () )
        {
            return title;
        }

        return String.format ( "Package Drone Channel: %s", id );
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
                if ( !processor.process ( artifact, streamer, this.context ) )
                {
                    // a processor can veto the process
                    break;
                }
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
