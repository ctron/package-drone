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
package org.eclipse.packagedrone.repo.adapter.p2.internal.aspect;

import static org.eclipse.packagedrone.repo.FileTypes.isXml;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import org.eclipse.packagedrone.repo.aspect.Constants;
import org.eclipse.packagedrone.repo.aspect.extract.Extractor;
import org.eclipse.packagedrone.utils.xml.XmlToolsFactory;

public class ExtractorImpl implements Extractor
{
    private final XmlToolsFactory xml;

    public ExtractorImpl ( final XmlToolsFactory xmlToolsFactory )
    {
        this.xml = xmlToolsFactory;
    }

    @Override
    public void extractMetaData ( final Extractor.Context context, final Map<String, String> metadata ) throws Exception
    {
        if ( !isXml ( context.getPath () ) )
        {
            return;
        }

        final String rootNodeName = getRootNodeName ( context.getPath () );

        if ( "artifacts".equals ( rootNodeName ) )
        {
            metadata.put ( "fragment", "true" );
            metadata.put ( "fragment-type", "artifacts" );
            metadata.put ( Constants.KEY_ARTIFACT_LABEL, "P2 Artifact Information" );
        }
        else if ( "units".equals ( rootNodeName ) )
        {
            metadata.put ( "fragment", "true" );
            metadata.put ( "fragment-type", "metadata" );
            metadata.put ( Constants.KEY_ARTIFACT_LABEL, "P2 Meta Data Fragment" );
        }
    }

    private String getRootNodeName ( final Path file ) throws Exception
    {
        final XMLInputFactory xin = this.xml.newXMLInputFactory ();

        try ( InputStream in = new BufferedInputStream ( Files.newInputStream ( file ) ) )
        {
            final XMLStreamReader reader = xin.createXMLStreamReader ( in );
            while ( reader.hasNext () )
            {
                if ( reader.nextTag () != XMLStreamConstants.START_ELEMENT )
                {
                    return null;
                }
                final QName name = reader.getName ();
                return name.getLocalPart ();
            }
        }
        return null;
    }

}
