/*******************************************************************************
 * Copyright (c) 2014 IBH SYSTEMS GmbH.
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dentrassi.pm.aspect.common.osgi.OsgiExtractor;
import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.common.servlet.Handler;
import de.dentrassi.pm.storage.Artifact;
import de.dentrassi.pm.storage.Channel;
import de.dentrassi.pm.storage.service.util.DownloadHelper;

public class DownloadHandler implements Handler
{
    private final static Logger logger = LoggerFactory.getLogger ( DownloadHandler.class );

    private final Channel channel;

    private final String filename;

    private final String classifier;

    private static final Pattern PATTERN = Pattern.compile ( "(.*)_(.*?)\\.jar" );

    public DownloadHandler ( final Channel channel, final String filename, final String classifier )
    {
        this.channel = channel;
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
        final Matcher m = PATTERN.matcher ( this.filename );
        if ( !m.matches () )
        {
            logger.warn ( "Invalid syntax: {}", this.filename );
            resp.setStatus ( HttpServletResponse.SC_NOT_FOUND );
            return;
        }

        final String name = m.group ( 1 );
        final String version = m.group ( 2 );
        logger.debug ( "Looking for bundle: {}/{}", name, version );

        // TODO: speed up search
        for ( final Artifact a : this.channel.getArtifacts () )
        {
            final Map<MetaKey, String> md = a.getInformation ().getMetaData ();

            final String thisClassifier = md.get ( new MetaKey ( "osgi", OsgiExtractor.KEY_CLASSIFIER ) );
            final String thisName = md.get ( new MetaKey ( "osgi", OsgiExtractor.KEY_NAME ) );
            final String thisVersion = md.get ( new MetaKey ( "osgi", OsgiExtractor.KEY_VERSION ) );

            logger.debug ( "This - name: {}, version: {}, classifier: {}", thisName, thisVersion, thisClassifier );

            if ( thisClassifier == null || !thisClassifier.equals ( this.classifier ) )
            {
                continue;
            }

            if ( thisName == null || !thisName.equals ( name ) )
            {
                continue;
            }

            if ( thisVersion == null || !thisVersion.equals ( version ) )
            {
                continue;
            }

            logger.debug ( "Streaming artifact: {} / {} ", a.getInformation ().getName (), a.getId () );
            DownloadHelper.streamArtifact ( resp, a, md.get ( new MetaKey ( "mime", "type" ) ), true );
            return;
        }

        logger.warn ( "Artifact not found - name: {}, version: {}, classifier: {}", name, version, this.classifier );
        resp.setStatus ( HttpServletResponse.SC_NOT_FOUND );
    }
}
