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
import java.nio.file.Files;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.dentrassi.pm.aspect.ChannelAspect;
import de.dentrassi.pm.aspect.extract.Extractor;
import de.dentrassi.pm.common.XmlHelper;

public class MavenPomExtractor implements Extractor
{
    private static final String NS = "http://maven.apache.org/POM/4.0.0";

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
    public void extractMetaData ( final Extractor.Context context, final Map<String, String> metadata ) throws Exception
    {
        final String probe = Files.probeContentType ( context.getPath () );

        if ( !"application/xml".equals ( probe ) )
        {
            return;
        }

        try ( BufferedInputStream in = new BufferedInputStream ( new FileInputStream ( context.getPath ().toFile () ) ) )
        {
            final Document doc = this.xml.parse ( in );
            final Element root = doc.getDocumentElement ();

            if ( !root.getNodeName ().equals ( "project" ) )
            {
                return;
            }

            final String ns = root.getNamespaceURI ();
            if ( ns != null && !ns.equals ( NS ) )
            {
                context.validationInformation ( "Ignoring POM file: The namespace set but is not: " + NS );
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
                context.validationInformation ( "Ignoring POM file: There is no group id" );
                return;
            }

            if ( artifactId == null || artifactId.isEmpty () )
            {
                context.validationInformation ( "Ignoring POM file: There is no artifact id" );
                return;
            }

            if ( version == null || version.isEmpty () )
            {
                context.validationInformation ( "Ignoring POM file: There is no version" );
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
