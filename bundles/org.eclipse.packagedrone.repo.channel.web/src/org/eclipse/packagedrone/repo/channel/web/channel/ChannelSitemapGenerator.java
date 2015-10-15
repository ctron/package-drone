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
package org.eclipse.packagedrone.repo.channel.web.channel;

import static com.google.common.net.UrlEscapers.urlPathSegmentEscaper;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

import java.time.Instant;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.packagedrone.repo.channel.ArtifactInformation;
import org.eclipse.packagedrone.repo.channel.ChannelInformation;
import org.eclipse.packagedrone.repo.channel.ChannelNotFoundException;
import org.eclipse.packagedrone.repo.channel.ChannelService;
import org.eclipse.packagedrone.repo.channel.ChannelService.By;
import org.eclipse.packagedrone.repo.channel.ReadableChannel;
import org.eclipse.packagedrone.repo.web.sitemap.ChangeFrequency;
import org.eclipse.packagedrone.repo.web.sitemap.SitemapContextCreator;
import org.eclipse.packagedrone.repo.web.sitemap.SitemapGenerator;
import org.eclipse.packagedrone.repo.web.sitemap.SitemapIndexContext;
import org.eclipse.packagedrone.repo.web.sitemap.UrlSetContext;

public class ChannelSitemapGenerator implements SitemapGenerator
{
    private final int threshold = 10_000;

    private final int hashSize = 8;

    private ChannelService channelService;

    public void setChannelService ( final ChannelService channelService )
    {
        this.channelService = channelService;
    }

    @Override
    public void gatherRoots ( final SitemapIndexContext context )
    {
        context.addLocation ( "channels", calcLastMod () );
    }

    @Override
    public void render ( final String path, final SitemapContextCreator creator )
    {
        final String[] toks = path.split ( "/" );

        if ( toks.length == 1 && "channels".equals ( toks[0] ) )
        {
            // write sitemap index for all channels

            final SitemapIndexContext context = creator.createSitemapIndex ();

            for ( final ChannelInformation ci : this.channelService.list () )
            {
                final String id = urlPathSegmentEscaper ().escape ( ci.getId () );
                final Optional<Instant> lastMod = ofNullable ( ci.getState ().getModificationTimestamp () );

                context.addLocation ( String.format ( "channels/%s", id ), lastMod );
            }
        }
        else if ( toks.length == 2 && "channels".equals ( toks[0] ) )
        {
            // sitemap index for channel

            final String channelId = toks[1];
            final Optional<ChannelInformation> state = this.channelService.getState ( By.id ( channelId ) );
            final Optional<Instant> lastMod = ofNullable ( state.get ().getState ().getModificationTimestamp () );

            final String id = urlPathSegmentEscaper ().escape ( channelId );

            final SitemapIndexContext context = creator.createSitemapIndex ();
            context.addLocation ( String.format ( "channels/%s/main", id ), lastMod );
            context.addLocation ( String.format ( "channels/%s/artifacts", id ), lastMod );
        }
        else if ( toks.length == 3 && "channels".equals ( toks[0] ) && "main".equals ( toks[2] ) )
        {
            // write sitemap indexes for one channel

            final String channelId = toks[1];
            final Optional<ChannelInformation> state = this.channelService.getState ( By.id ( channelId ) );

            final Optional<Instant> lastMod = ofNullable ( state.get ().getState ().getModificationTimestamp () );

            final String id = urlPathSegmentEscaper ().escape ( channelId );

            final UrlSetContext context = creator.createUrlSet ();

            if ( state.isPresent () )
            {
                context.addLocation ( String.format ( "/channel/%s/view", id ), lastMod, of ( ChangeFrequency.DAILY ), empty () );
                context.addLocation ( String.format ( "/channel/%s/viewPlain", id ), lastMod, of ( ChangeFrequency.DAILY ), empty () );
                context.addLocation ( String.format ( "/channel/%s/details", id ), lastMod, of ( ChangeFrequency.DAILY ), empty () );
                context.addLocation ( String.format ( "/channel/%s/validation", id ), lastMod, of ( ChangeFrequency.DAILY ), empty () );
            }
        }
        else if ( toks.length == 3 && "channels".equals ( toks[0] ) && "artifacts".equals ( toks[2] ) )
        {
            final String channelId = toks[1];

            final List<ArtifactInformation> arts = getArtifacts ( channelId, null );

            if ( arts.size () < this.threshold )
            {
                renderArtifacts ( creator.createUrlSet (), channelId, arts );
            }
            else
            {
                renderSubIndex ( creator.createSitemapIndex (), channelId, "", arts );
            }
        }
        else if ( toks.length == 4 && "channels".equals ( toks[0] ) && "artifacts".equals ( toks[2] ) )
        {
            final String channelId = toks[1];
            final String artifactHashPrefix = toks[3];

            final List<ArtifactInformation> arts = getArtifacts ( channelId, ( art ) -> makeHash ( art ).startsWith ( artifactHashPrefix ) );

            if ( arts.size () < this.threshold || artifactHashPrefix.length () + 3 >= this.hashSize )
            {
                renderArtifacts ( creator.createUrlSet (), channelId, arts );
            }
            else
            {
                renderSubIndex ( creator.createSitemapIndex (), channelId, artifactHashPrefix, arts );
            }
        }
    }

    private List<ArtifactInformation> getArtifacts ( final String channelId, final Predicate<ArtifactInformation> filter )
    {
        try
        {
            return this.channelService.accessCall ( By.id ( channelId ), ReadableChannel.class, channel -> {
                Stream<ArtifactInformation> s = channel.getArtifacts ().stream ();

                if ( filter != null )
                {
                    s = s.filter ( filter );
                }

                return s.sorted ( Comparator.comparing ( ArtifactInformation::getId ) ).collect ( Collectors.toList () );
            } );
        }
        catch ( final ChannelNotFoundException e )
        {
            return null;
        }
    }

    private void renderSubIndex ( final SitemapIndexContext context, final String channelId, final String existingPrefix, final List<ArtifactInformation> artifacts )
    {
        final String cid = urlPathSegmentEscaper ().escape ( channelId );

        final Set<String> prefixSet = new HashSet<> ( 4096 );

        final int existingLength = existingPrefix.length ();

        for ( final ArtifactInformation art : artifacts )
        {
            final String hash = makeHash ( art );

            if ( !hash.startsWith ( existingPrefix ) )
            {
                continue;
            }

            final String prefix = hash.substring ( 0, existingLength + 3 );

            if ( prefixSet.add ( prefix ) )
            {
                context.addLocation ( String.format ( "channels/%s/artifacts/%s", cid, prefix ), empty () );
            }
        }
    }

    private void renderArtifacts ( final UrlSetContext context, final String channelId, final List<ArtifactInformation> artifacts )
    {
        final String cid = urlPathSegmentEscaper ().escape ( channelId );

        for ( final ArtifactInformation art : artifacts )
        {
            final String aid = urlPathSegmentEscaper ().escape ( art.getId () );

            context.addLocation ( String.format ( "/channel/%s/artifacts/%s/view", cid, aid ), ofNullable ( art.getCreationInstant () ), of ( ChangeFrequency.WEEKLY ), empty () );
        }
    }

    private String makeHash ( final ArtifactInformation art )
    {
        return String.format ( "%08x", art.getId ().hashCode () );
    }

    /**
     * Find the last modification timestamp of all channels
     *
     * @return the latest modification timestamp of all channels
     */
    private Optional<Instant> calcLastMod ()
    {
        Instant globalLastMod = null;

        for ( final ChannelInformation ci : this.channelService.list () )
        {
            final Optional<Instant> lastMod = ofNullable ( ci.getState ().getModificationTimestamp () );

            if ( globalLastMod == null || lastMod.get ().isAfter ( globalLastMod ) )
            {
                globalLastMod = lastMod.get ();
            }
        }
        return Optional.ofNullable ( globalLastMod );
    }

}
