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
import org.w3c.dom.ProcessingInstruction;

import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.common.XmlHelper;
import de.dentrassi.pm.storage.Artifact;
import de.dentrassi.pm.storage.Channel;

public class MetadataHandler extends AbstractRepositoryHandler
{
    public MetadataHandler ( final Channel channel, final boolean compress )
    {
        super ( channel, compress, "content" );
    }

    @Override
    public void prepare () throws Exception
    {
        final Document doc = this.xml.create ();

        final ProcessingInstruction pi = doc.createProcessingInstruction ( "metadataRepository", "version=\"1.1.0\"" );
        doc.appendChild ( pi );

        final Element root = doc.createElement ( "repository" );
        doc.appendChild ( root );
        root.setAttribute ( "name", String.format ( "Package Drone - Channel: %s", this.channel.getId () ) );
        root.setAttribute ( "type", "org.eclipse.equinox.internal.p2.metadata.repository.LocalMetadataRepository" );
        root.setAttribute ( "version", "1" );

        addProperties ( root );

        final Element units = addElement ( root, "units" );

        for ( final Artifact artifact : this.channel.getArtifacts () )
        {
            final String ft = artifact.getMetaData ().get ( new MetaKey ( "p2.repo", "fragment-type" ) );

            if ( "metadata".equals ( ft ) )
            {
                attachP2Metadata ( artifact, units );
            }
        }

        fixSize ( units );

        setData ( doc );
    }

    private void attachP2Metadata ( final Artifact artifact, final Element units )
    {
        artifact.streamData ( ( info, stream ) -> {
            final Document mdoc = this.xml.parse ( stream );
            for ( final Node node : XmlHelper.iter ( this.xml.path ( mdoc, "//unit" ) ) )
            {
                final Node nn = units.getOwnerDocument ().adoptNode ( node.cloneNode ( true ) );
                units.appendChild ( nn );
            }
        } );
    }
}
