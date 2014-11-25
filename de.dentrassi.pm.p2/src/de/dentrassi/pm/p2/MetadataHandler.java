package de.dentrassi.pm.p2;

import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;

import de.dentrassi.pm.common.XmlHelper;
import de.dentrassi.pm.storage.service.Artifact;
import de.dentrassi.pm.storage.service.Channel;
import de.dentrassi.pm.storage.service.MetaKey;

public class MetadataHandler extends AbstractRepositoryHandler
{
    public MetadataHandler ( final Channel channel )
    {
        super ( channel );
    }

    @Override
    public void prepare () throws Exception
    {
        final Document doc = this.xml.create ();

        {
            final ProcessingInstruction pi = doc.createProcessingInstruction ( "metadataRepository", "version=\"1.1.0\"" );
            doc.appendChild ( pi );
        }

        final Element root = doc.createElement ( "repository" );
        doc.appendChild ( root );
        root.setAttribute ( "name", String.format ( "Package Drone - Channel: %s", this.channel.getId () ) );
        root.setAttribute ( "type", "org.eclipse.equinox.internal.p2.metadata.repository.LocalMetadataRepository" );
        root.setAttribute ( "version", "1" );

        addProperties ( root );

        final Element units = addElement ( root, "units" );

        for ( final Artifact artifact : this.channel.getArtifacts () )
        {
            final Map<MetaKey, String> md = artifact.getMetaData ();
            if ( "p2metadata".equals ( md.get ( new MetaKey ( "mvn", "classifier" ) ) ) )
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
