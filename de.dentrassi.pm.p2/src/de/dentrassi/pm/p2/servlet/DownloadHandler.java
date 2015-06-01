/*******************************************************************************
 * Copyright (c) 2014, 2015 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package de.dentrassi.pm.p2.servlet;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dentrassi.pm.aspect.common.osgi.OsgiAspectFactory;
import de.dentrassi.pm.aspect.common.osgi.OsgiExtractor;
import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.common.servlet.Handler;
import de.dentrassi.pm.storage.Artifact;
import de.dentrassi.pm.storage.Channel;
import de.dentrassi.pm.storage.service.util.DownloadHelper;

public class DownloadHandler implements Handler
{
    private final static Logger logger = LoggerFactory.getLogger ( DownloadHandler.class );

    private static final MetaKey KEY_MIME_TYPE = new MetaKey ( "mime", "type" );

    private static final MetaKey KEY_OSGI_CLASSIFIER = new MetaKey ( OsgiAspectFactory.ID, OsgiExtractor.KEY_CLASSIFIER );

    private static final MetaKey KEY_OSGI_ID = new MetaKey ( OsgiAspectFactory.ID, OsgiExtractor.KEY_NAME );

    private static final MetaKey KEY_OSGI_VERSION = new MetaKey ( OsgiAspectFactory.ID, OsgiExtractor.KEY_VERSION );

    private final Channel channel;

    private final String id;

    private final String version;

    private final String filename;

    private final String classifier;

    public DownloadHandler ( final Channel channel, final String id, final String version, final String filename, final String classifier )
    {
        this.channel = channel;
        this.id = id;
        this.version = version;
        this.filename = filename;
        this.classifier = classifier;
    }

    @Override
    public void prepare () throws Exception
    {
    }

    @Override
    public void process ( final HttpServletRequest req, final HttpServletResponse resp ) throws Exception
    {
        logger.debug ( "Looking for bundle: {}/{}", this.id, this.version );

        // TODO: speed up search
        for ( final Artifact a : this.channel.getArtifacts () )
        {
            final Map<MetaKey, String> md = a.getInformation ().getMetaData ();

            final String thisClassifier = md.get ( KEY_OSGI_CLASSIFIER );
            final String thisId = md.get ( KEY_OSGI_ID );
            final String thisVersion = md.get ( KEY_OSGI_VERSION );

            logger.debug ( "This - id: {}, version: {}, classifier: {}", thisId, thisVersion, thisClassifier );

            if ( thisClassifier == null || !thisClassifier.equals ( this.classifier ) )
            {
                continue;
            }

            if ( thisId == null || !thisId.equals ( this.id ) )
            {
                continue;
            }

            if ( thisVersion == null || !thisVersion.equals ( this.version ) )
            {
                continue;
            }

            logger.debug ( "Streaming artifact: {} / {} ", a.getInformation ().getName (), a.getId () );
            DownloadHelper.streamArtifact ( resp, a, md.get ( KEY_MIME_TYPE ), true, art -> this.filename );
            return;
        }

        final String message = String.format ( "Artifact not found - name: %s, version: %s, classifier: %s", this.id, this.version, this.classifier );
        logger.warn ( message );
        resp.setStatus ( HttpServletResponse.SC_NOT_FOUND );
        resp.setContentType ( "text/plain" );
        resp.getWriter ().println ( message );
    }
}
