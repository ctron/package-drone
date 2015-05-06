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

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import de.dentrassi.pm.common.ArtifactInformation;
import de.dentrassi.pm.common.XmlHelper;

public class ChecksumValidatorProcessor implements Processor
{
    private final static Logger logger = LoggerFactory.getLogger ( ChecksumValidatorProcessor.class );

    private final XmlHelper xml = new XmlHelper ();

    private final Multimap<String, String> checksums = HashMultimap.create ();

    private final Multimap<String, String> checksumArtifacts = HashMultimap.create ();

    @Override
    public void process ( final ArtifactInformation artifact, final ArtifactStreamer streamer ) throws Exception
    {
        final String ft = artifact.getMetaData ().get ( ArtifactsProcessor.MK_FRAGMENT_TYPE );

        if ( "artifacts".equals ( ft ) )
        {
            processP2Artifact ( artifact, streamer );
        }
    }

    private void processP2Artifact ( final ArtifactInformation artifact, final ArtifactStreamer streamer ) throws Exception
    {
        streamer.stream ( artifact.getId (), ( info, stream ) -> {
            final Document mdoc = this.xml.parse ( stream );
            for ( final Node node : XmlHelper.iter ( this.xml.path ( mdoc, "//artifact" ) ) )
            {
                if ( ! ( node instanceof Element ) )
                {
                    continue;
                }

                recordArtifact ( artifact, (Element)node );
            }
        } );
    }

    /**
     * Record the artifact for duplicate detection
     *
     * @param artifact
     *            the current artifact
     * @param node
     *            the artifact node
     */
    private void recordArtifact ( final ArtifactInformation artifact, final Element ele )
    {
        final String classifier = ele.getAttribute ( "classifier" );
        final String id = ele.getAttribute ( "id" );
        final String version = ele.getAttribute ( "version" );

        final String key = String.format ( "%s::%s::%s", classifier, id, version );

        String value;
        try
        {
            value = this.xml.getElementValue ( ele, "./properties/property[@name='download.md5']/@value" );
        }
        catch ( final Exception e )
        {
            throw new RuntimeException ( e );
        }

        if ( value == null || value.isEmpty () )
        {
            logger.debug ( "Artifact {} did not have a checksum", key );
            return;
        }

        this.checksums.put ( key, value );
        this.checksumArtifacts.put ( fullKey ( key, value ), artifact.getId () );

        logger.debug ( "Recording artifact - id: {}, md5: {}, artifact: {}", key, value, artifact.getId () );
    }

    /**
     * Make a list of all installable units which have conflicting MD5 checksums
     *
     * @return a map of the conflicting MD5 checksums, each set in the list is
     *         one group of artifacts which share the same key but have
     *         different checksums. The key is the combination of classifier, id
     *         and version.
     */
    public Map<String, Set<String>> checkDuplicates ()
    {
        final Map<String, Set<String>> result = new HashMap<> ();

        for ( final Map.Entry<String, Collection<String>> entry : this.checksums.asMap ().entrySet () )
        {
            if ( entry.getValue ().size () < 2 )
            {
                continue;
            }

            final Set<String> artifacts = new HashSet<> ();

            for ( final String value : entry.getValue () )
            {
                artifacts.addAll ( this.checksumArtifacts.get ( fullKey ( entry.getKey (), value ) ) );
            }

            result.put ( entry.getKey (), artifacts );
        }

        return result;
    }

    private static String fullKey ( final String key, final String checksum )
    {
        return key + "::" + checksum;
    }

    @Override
    public void write ( final OutputStream stream ) throws IOException
    {
    }

    @Override
    public String getId ()
    {
        return null;
    }

}
