/*******************************************************************************
 * Copyright (c) 2015 Jens Reimann.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jens Reimann - initial API and implementation
 *******************************************************************************/
package de.dentrassi.pm.npm;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.npm.aspect.NpmChannelAspectFactory;
import de.dentrassi.pm.storage.Artifact;
import de.dentrassi.pm.storage.Channel;
import de.dentrassi.pm.system.SystemService;

public class ModuleHandler
{
    private final static Logger logger = LoggerFactory.getLogger ( ModuleHandler.class );

    private final Channel channel;

    private final String moduleName;

    private final boolean pretty;

    private final SystemService service;

    public ModuleHandler ( final SystemService service, final Channel channel, final String moduleName, final boolean pretty )
    {
        this.service = service;
        this.channel = channel;
        this.moduleName = moduleName;
        this.pretty = pretty;
    }

    public void process ( final HttpServletResponse response ) throws IOException
    {
        response.setContentType ( "application/json" );
        process ( response.getOutputStream () );
    }

    private static class PackageEntry
    {
        private final JsonElement element;

        private final PackageInfo info;

        private final Artifact artifact;

        public PackageEntry ( final PackageInfo info, final JsonElement element, final Artifact artifact )
        {
            this.info = info;
            this.element = element;
            this.artifact = artifact;
        }

        public Artifact getArtifact ()
        {
            return this.artifact;
        }

        public JsonElement getElement ()
        {
            return this.element;
        }

        public PackageInfo getInfo ()
        {
            return this.info;
        }
    }

    public void process ( final OutputStream stream ) throws IOException
    {
        final String sitePrefix = this.service.getDefaultSitePrefix ();

        final GsonBuilder builder = new GsonBuilder ();
        if ( this.pretty )
        {
            builder.setPrettyPrinting ();
        }

        builder.setDateFormat ( "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'" );

        final Gson gson = builder.create ();

        final JsonParser parser = new JsonParser ();

        final TreeMap<String, PackageEntry> versions = new TreeMap<> ();

        for ( final Artifact art : this.channel.getArtifacts () )
        {
            final String pkg = art.getInformation ().getMetaData ().get ( new MetaKey ( NpmChannelAspectFactory.ID, "package.json" ) );
            if ( pkg == null )
            {
                continue;
            }

            try
            {
                final JsonElement pkgEle = parser.parse ( pkg );
                final PackageInfo pi = gson.fromJson ( pkgEle, PackageInfo.class );

                if ( !this.moduleName.equals ( pi.getName () ) )
                {
                    continue;
                }

                versions.put ( pi.getVersion (), new PackageEntry ( pi, pkgEle, art ) );
            }
            catch ( final Exception e )
            {
                logger.info ( "Failed to parse package.json of " + art.getId (), e );
                continue;
            }
        }

        // now build the main file

        final JsonObject main = new JsonObject ();

        // pull in meta data from most recent version

        main.addProperty ( "name", this.moduleName );

        if ( !versions.isEmpty () )
        {
            final Entry<String, PackageEntry> mostRecent = versions.lastEntry ();
            final PackageInfo pi = mostRecent.getValue ().getInfo ();
            main.addProperty ( "license", pi.getLicense () );

            final JsonObject distTags = new JsonObject ();
            main.add ( "dist-tags", distTags );

            distTags.addProperty ( "latest", pi.getVersion () );
        }

        final JsonObject times = new JsonObject ();
        main.add ( "time", times );

        final JsonObject versionsEle = new JsonObject ();
        main.add ( "versions", versionsEle );

        for ( final Map.Entry<String, PackageEntry> entry : versions.entrySet () )
        {
            final PackageInfo pi = entry.getValue ().getInfo ();
            final Artifact art = entry.getValue ().getArtifact ();

            times.add ( pi.getVersion (), gson.toJsonTree ( art.getInformation ().getCreationTimestamp () ) );

            final JsonObject ele = (JsonObject)entry.getValue ().getElement ();

            final JsonObject dist = new JsonObject ();
            dist.addProperty ( "shasum", art.getInformation ().getMetaData ().get ( new MetaKey ( "hasher", "sha1" ) ) );
            dist.addProperty ( "tarball", String.format ( "%s/artifact/%s/dump", sitePrefix, art.getId () ) );

            ele.add ( "dist", dist );

            versionsEle.add ( pi.getVersion (), ele );
        }

        // render

        try ( OutputStreamWriter out = new OutputStreamWriter ( stream, StandardCharsets.UTF_8 ) )
        {
            gson.toJson ( main, out );
        }
    }
}
