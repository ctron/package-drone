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

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.dentrassi.pm.aspect.ChannelAspect;
import de.dentrassi.pm.aspect.extract.Extractor;
import de.dentrassi.pm.common.XmlHelper;

public class ExtractorImpl implements Extractor
{

    private final XmlHelper xml = new XmlHelper ();

    private final ChannelAspect channelAspect;

    public ExtractorImpl ( final ChannelAspect channelAspect )
    {
        this.channelAspect = channelAspect;
    }

    @Override
    public ChannelAspect getAspect ()
    {
        return this.channelAspect;
    }

    @Override
    public void extractMetaData ( final Path file, final Map<String, String> metadata ) throws Exception
    {
        final String probe = Files.probeContentType ( file );

        if ( !probe.equals ( "application/xml" ) )
        {
            return;
        }

        if ( isArtifacts ( file ) )
        {
            metadata.put ( "fragment", "true" );
            metadata.put ( "fragment-type", "artifacts" );
        }
        else if ( isMetaData ( file ) )
        {
            metadata.put ( "fragment", "true" );
            metadata.put ( "fragment-type", "metadata" );
        }
    }

    private boolean isArtifacts ( final Path file )
    {
        try
        {
            try ( InputStream in = new BufferedInputStream ( new FileInputStream ( file.toFile () ) ) )
            {
                final Document doc = this.xml.parse ( in );
                final Element root = doc.getDocumentElement ();
                if ( root.getNodeName ().equals ( "artifacts" ) )
                {
                    return true;
                }
            }
        }
        catch ( final Exception e )
        {
        }

        return false;
    }

    private boolean isMetaData ( final Path file ) throws Exception
    {
        try
        {
            try ( InputStream in = new BufferedInputStream ( new FileInputStream ( file.toFile () ) ) )
            {
                final Document doc = this.xml.parse ( in );
                final Element root = doc.getDocumentElement ();
                if ( root.getNodeName ().equals ( "units" ) )
                {
                    return true;
                }
            }
        }
        catch ( final Exception e )
        {
        }

        return false;
    }

}
