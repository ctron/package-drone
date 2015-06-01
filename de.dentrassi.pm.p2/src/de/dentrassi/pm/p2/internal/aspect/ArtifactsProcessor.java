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

import static de.dentrassi.pm.common.XmlHelper.addElement;
import static de.dentrassi.pm.common.XmlHelper.fixSize;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import de.dentrassi.pm.common.ArtifactInformation;
import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.common.XmlHelper;

public class ArtifactsProcessor extends AbstractRepositoryProcessor
{
    private final static Logger logger = LoggerFactory.getLogger ( ArtifactsProcessor.class );

    public static final MetaKey MK_FRAGMENT_TYPE = new MetaKey ( "p2.repo", "fragment-type" );

    private final Element artifacts;

    private final Document doc;

    public ArtifactsProcessor ( final String title, final boolean compressed )
    {
        super ( title, "artifacts", compressed );

        this.doc = initRepository ( "artifactRepository", "org.eclipse.equinox.p2.artifact.repository.simpleRepository" );
        final Element root = this.doc.getDocumentElement ();

        addProperties ( root );
        addMappings ( root );

        this.artifacts = addElement ( root, "artifacts" );
    }

    private void addMappings ( final Element root )
    {
        final Element mappings = addElement ( root, "mappings" );

        addMapping ( mappings, "(& (classifier=osgi.bundle))", "${repoUrl}/plugins/${id}/${version}/${id}_${version}.jar" );
        addMapping ( mappings, "(& (classifier=binary))", "${repoUrl}/binary/${id}/${version}/${id}_${version}" );
        addMapping ( mappings, "(& (classifier=org.eclipse.update.feature))", "${repoUrl}/features/${id}/${version}/${id}_${version}.jar" );

        fixSize ( mappings );
    }

    private void addMapping ( final Element mappings, final String rule, final String output )
    {
        final Element m = addElement ( mappings, "rule" );
        m.setAttribute ( "filter", rule );
        m.setAttribute ( "output", output );
    }

    @Override
    public boolean process ( final ArtifactInformation artifact, final ArtifactStreamer streamer, final Map<String, Object> context ) throws Exception
    {
        final String ft = artifact.getMetaData ().get ( MK_FRAGMENT_TYPE );

        if ( "artifacts".equals ( ft ) )
        {
            attachP2Artifact ( artifact, this.artifacts, streamer, context );
        }

        return true;
    }

    private void attachP2Artifact ( final ArtifactInformation artifact, final Element artifacts, final ArtifactStreamer streamer, final Map<String, Object> context ) throws Exception
    {
        streamer.stream ( artifact.getId (), ( info, stream ) -> {
            final Document mdoc = this.xml.parse ( stream );
            for ( final Node node : XmlHelper.iter ( this.xml.path ( mdoc, "//artifact" ) ) )
            {
                if ( ! ( node instanceof Element ) )
                {
                    continue;
                }

                final String key = ChecksumValidatorProcessor.makeKey ( (Element)node );
                if ( ChecksumValidatorProcessor.shouldSkip ( context, key ) )
                {
                    logger.trace ( "IU {} of artifact {} should be skipped", key, artifact.getId () );
                    continue;
                }

                final Node nn = artifacts.getOwnerDocument ().adoptNode ( node.cloneNode ( true ) );
                artifacts.appendChild ( nn );
            }
        } );
    }

    @Override
    public void write ( final OutputStream stream ) throws IOException
    {
        fixSize ( this.artifacts );
        write ( this.doc, stream );
    }

}
