/*******************************************************************************
 * Copyright (c) 2014 Jens Reimann.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jens Reimann - initial API and implementation
 *******************************************************************************/
package de.dentrassi.pm.p2.servlet;

import static de.dentrassi.pm.common.XmlHelper.fixSize;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.common.XmlHelper;
import de.dentrassi.pm.storage.Artifact;
import de.dentrassi.pm.storage.Channel;

public class ArtifactsHandler extends AbstractRepositoryHandler
{
    public ArtifactsHandler ( final Channel channel, final boolean compress )
    {
        super ( channel, compress, "artifacts" );
    }

    @Override
    public void prepare () throws Exception
    {
        final Document doc = initRepository ( "artifactRepository", "org.eclipse.equinox.p2.artifact.repository.simpleRepository" );
        final Element root = doc.getDocumentElement ();

        addProperties ( root );
        addMappings ( root );

        final Element artifacts = addElement ( root, "artifacts" );

        for ( final Artifact artifact : this.channel.getArtifacts () )
        {
            final String ft = artifact.getMetaData ().get ( new MetaKey ( "p2.repo", "fragment-type" ) );

            if ( "artifacts".equals ( ft ) )
            {
                attachP2Artifact ( artifact, artifacts );
            }
        }

        fixSize ( artifacts );

        setData ( doc );
    }

    private void attachP2Artifact ( final Artifact artifact, final Element artifacts )
    {
        artifact.streamData ( ( info, stream ) -> {
            final Document mdoc = this.xml.parse ( stream );
            for ( final Node node : XmlHelper.iter ( this.xml.path ( mdoc, "//artifact" ) ) )
            {
                final Node nn = artifacts.getOwnerDocument ().adoptNode ( node.cloneNode ( true ) );
                artifacts.appendChild ( nn );
            }
        } );
    }

    private void addMappings ( final Element root )
    {
        final Element mappings = addElement ( root, "mappings" );

        addMapping ( mappings, "(& (classifier=osgi.bundle))", "${repoUrl}/plugins/${id}_${version}.jar" );
        addMapping ( mappings, "(& (classifier=binary))", "${repoUrl}/binary/${id}_${version}" );
        addMapping ( mappings, "(& (classifier=org.eclipse.update.feature))", "${repoUrl}/features/${id}_${version}.jar" );

        fixSize ( mappings );
    }

    private void addMapping ( final Element mappings, final String rule, final String output )
    {
        final Element m = addElement ( mappings, "rule" );
        m.setAttribute ( "filter", rule );
        m.setAttribute ( "output", output );
    }

}
