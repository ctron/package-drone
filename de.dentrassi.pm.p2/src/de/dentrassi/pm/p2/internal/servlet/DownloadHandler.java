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
package de.dentrassi.pm.p2.internal.servlet;

import static java.util.Optional.empty;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dentrassi.pm.aspect.common.osgi.OsgiAspectFactory;
import de.dentrassi.pm.aspect.common.osgi.OsgiExtractor;
import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.common.servlet.Handler;
import de.dentrassi.pm.storage.channel.ArtifactInformation;
import de.dentrassi.pm.storage.channel.ChannelService;
import de.dentrassi.pm.storage.channel.ChannelService.By;
import de.dentrassi.pm.storage.channel.ReadableChannel;
import de.dentrassi.pm.storage.channel.util.DownloadHelper;

public class DownloadHandler implements Handler
{
    private final static Logger logger = LoggerFactory.getLogger ( DownloadHandler.class );

    private static final MetaKey KEY_OSGI_CLASSIFIER = new MetaKey ( OsgiAspectFactory.ID, OsgiExtractor.KEY_CLASSIFIER );

    private static final MetaKey KEY_OSGI_ID = new MetaKey ( OsgiAspectFactory.ID, OsgiExtractor.KEY_NAME );

    private static final MetaKey KEY_OSGI_VERSION = new MetaKey ( OsgiAspectFactory.ID, OsgiExtractor.KEY_VERSION );

    private final String channelId;

    private final String id;

    private final String version;

    private final String filename;

    private final String classifier;

    private final ChannelService service;

    public DownloadHandler ( final String channelId, final ChannelService service, final String id, final String version, final String filename, final String classifier )
    {
        this.channelId = channelId;
        this.service = service;
        this.id = id;
        this.version = version;
        this.filename = filename;
        this.classifier = classifier;
    }

    @Override
    public void process ( final HttpServletRequest req, final HttpServletResponse resp ) throws IOException
    {
        logger.debug ( "Looking for bundle: {}/{}", this.id, this.version );

        this.service.accessRun ( By.id ( this.channelId ), ReadableChannel.class, channel -> {

            // TODO: speed up search
            for ( final ArtifactInformation a : channel.getContext ().getArtifacts ().values () )
            {
                final Map<MetaKey, String> md = a.getMetaData ();

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

                logger.debug ( "Streaming artifact: {} / {} ", a.getName (), a.getId () );
                DownloadHelper.streamArtifact ( resp, this.service, this.channelId, a.getId (), empty (), true, art -> this.filename );
                return; // exit search loop
            }

            // handle - not found

            final String message = String.format ( "Artifact not found - name: %s, version: %s, classifier: %s", this.id, this.version, this.classifier );
            logger.warn ( message );
            resp.setStatus ( HttpServletResponse.SC_NOT_FOUND );
            resp.setContentType ( "text/plain" );
            resp.getWriter ().println ( message );
        } );

    }
}
