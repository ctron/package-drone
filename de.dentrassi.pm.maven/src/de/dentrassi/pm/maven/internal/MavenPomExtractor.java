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
package de.dentrassi.pm.maven.internal;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.dentrassi.pm.aspect.ChannelAspect;
import de.dentrassi.pm.aspect.extract.Extractor;
import de.dentrassi.pm.common.XmlHelper;

public class MavenPomExtractor implements Extractor
{
    private final ChannelAspect aspect;

    private final XmlHelper xml = new XmlHelper ();

    public MavenPomExtractor ( final ChannelAspect aspect )
    {
        this.aspect = aspect;
    }

    @Override
    public ChannelAspect getAspect ()
    {
        return this.aspect;
    }

    @Override
    public void extractMetaData ( final Path file, final Map<String, String> metadata ) throws Exception
    {
        try ( BufferedInputStream in = new BufferedInputStream ( new FileInputStream ( file.toFile () ) ) )
        {
            final Document doc = this.xml.parse ( in );
            final Element root = doc.getDocumentElement ();

            if ( !root.getNodeName ().equals ( "project" ) )
            {
                return;
            }

            final String ns = root.getNamespaceURI ();
            if ( ns != null && !ns.equals ( "http://maven.apache.org/POM/4.0.0" ) )
            {
                return;
            }

            String groupId = this.xml.getElementValue ( root, "./groupId" );
            if ( groupId == null )
            {
                groupId = this.xml.getElementValue ( root, "./parent/groupId" );
            }

            final String artifactId = this.xml.getElementValue ( root, "./artifactId" );

            String version = this.xml.getElementValue ( root, "./version" );
            if ( version == null )
            {
                version = this.xml.getElementValue ( root, "./parent/version" );
            }

            if ( groupId == null || groupId.isEmpty () )
            {
                return;
            }

            if ( artifactId == null || artifactId.isEmpty () )
            {
                return;
            }

            if ( version == null || version.isEmpty () )
            {
                return;
            }

            metadata.put ( "groupId", groupId );
            metadata.put ( "artifactId", artifactId );
            metadata.put ( "version", version );
            metadata.put ( "extension", "pom" );
        }
        catch ( final Exception e )
        {
            // silently ignore
        }
    }

}
