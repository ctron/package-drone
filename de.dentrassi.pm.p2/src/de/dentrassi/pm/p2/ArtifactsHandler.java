package de.dentrassi.pm.p2;

import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import org.osgi.framework.Constants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.io.ByteStreams;

import de.dentrassi.pm.storage.service.Artifact;
import de.dentrassi.pm.storage.service.ArtifactInformation;
import de.dentrassi.pm.storage.service.Channel;
import de.dentrassi.pm.storage.service.MetaKey;

public class ArtifactsHandler extends AbstractRepositoryHandler
{
    public ArtifactsHandler ( final Channel channel )
    {
        super ( channel );
    }

    @Override
    public void prepare () throws Exception
    {
        final Document doc = initRepository ( "artifactRepository", "org.eclipse.equinox.p2.artifact.repository.simpleRepository" );
        final Element root = doc.getDocumentElement ();

        addProperties ( root );

        final Element artifacts = addElement ( root, "artifacts" );

        for ( final Artifact artifact : this.channel.getArtifacts () )
        {
            final Map<MetaKey, String> md = artifact.getMetaData ();

            if ( "jar".equals ( md.get ( new MetaKey ( "mvn", "extension" ) ) ) )
            {
                // need a different way to detect JARs
                attachP2Data ( artifact, artifacts, md );
            }
        }

        fixSize ( artifacts );

        setData ( doc );
    }

    private void attachP2Data ( final Artifact artifact, final Element artifacts, final Map<MetaKey, String> md ) throws IOException
    {
        final Path file = Files.createTempFile ( "blob-", null );
        try
        {
            artifact.streamData ( ( info, stream ) -> {
                try ( BufferedOutputStream out = new BufferedOutputStream ( new FileOutputStream ( file.toFile () ) ) )
                {
                    ByteStreams.copy ( stream, out );
                }
                try
                {
                    extractInformation ( file, artifacts, info, md );
                }
                catch ( final Exception e )
                {
                    // ignore
                }
            } );
        }
        finally
        {
            Files.deleteIfExists ( file );
        }
    }

    private void extractInformation ( final Path file, final Element artifacts, final ArtifactInformation info, final Map<MetaKey, String> md ) throws IOException
    {
        final Manifest mf;
        try ( final JarInputStream jarStream = new JarInputStream ( new FileInputStream ( file.toFile () ) ) )
        {
            mf = jarStream.getManifest ();
        }

        final String symbolicName = mf.getMainAttributes ().getValue ( Constants.BUNDLE_SYMBOLICNAME );
        final String version = mf.getMainAttributes ().getValue ( Constants.BUNDLE_VERSION );

        if ( symbolicName == null || version == null )
        {
            return;
        }

        final Element a = addElement ( artifacts, "artifact" );
        a.setAttribute ( "classifier", "osgi.bundle" );
        a.setAttribute ( "id", symbolicName );
        a.setAttribute ( "version", version );

        final String md5 = md.get ( new MetaKey ( "hasher", "md5" ) );

        final Element props = addElement ( a, "properties" );

        if ( md5 != null )
        {
            addProperty ( props, "download.md5", md5 );
        }

        addProperty ( props, "download.size", "" + info.getLength () );
        addProperty ( props, "artifact.size", "" + info.getLength () );

        fixSize ( props );
    }

    private void addProperty ( final Element props, final String key, final String value )
    {
        final Element p = addElement ( props, "property" );
        p.setAttribute ( "name", key );
        p.setAttribute ( "value", value );
    }
}
