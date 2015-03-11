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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;

import de.dentrassi.pm.common.ArtifactInformation;
import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.common.XmlHelper;

public class MetaDataProcessor extends AbstractRepositoryProcessor
{
    private static final MetaKey MK_FRAGMENT_TYPE = new MetaKey ( "p2.repo", "fragment-type" );

    private final Element units;

    private final Document doc;

    public MetaDataProcessor ( final String title, final boolean compressed )
    {
        super ( title, "content", compressed );

        this.doc = this.xml.create ();

        final ProcessingInstruction pi = this.doc.createProcessingInstruction ( "metadataRepository", "version=\"1.1.0\"" );
        this.doc.appendChild ( pi );

        final Element root = this.doc.createElement ( "repository" );
        this.doc.appendChild ( root );
        root.setAttribute ( "name", title );
        root.setAttribute ( "type", "org.eclipse.equinox.internal.p2.metadata.repository.LocalMetadataRepository" );
        root.setAttribute ( "version", "1" );

        addProperties ( root );

        this.units = addElement ( root, "units" );
    }

    @Override
    public void process ( final ArtifactInformation artifact, final ArtifactStreamer streamer ) throws Exception
    {
        final String ft = artifact.getMetaData ().get ( MK_FRAGMENT_TYPE );

        if ( "metadata".equals ( ft ) )
        {
            attachP2Artifact ( artifact, this.units, streamer );
        }
    }

    private void attachP2Artifact ( final ArtifactInformation artifact, final Element units2, final ArtifactStreamer streamer ) throws Exception
    {
        streamer.stream ( artifact.getId (), ( info, stream ) -> {
            final Document mdoc = this.xml.parse ( stream );
            for ( final Node node : XmlHelper.iter ( this.xml.path ( mdoc, "//unit" ) ) )
            {
                final Node nn = this.units.getOwnerDocument ().adoptNode ( node.cloneNode ( true ) );
                this.units.appendChild ( nn );
            }
        } );
    }

    @Override
    public void write ( final OutputStream stream ) throws IOException
    {
        fixSize ( this.units );
        write ( this.doc, stream );
    }

}
