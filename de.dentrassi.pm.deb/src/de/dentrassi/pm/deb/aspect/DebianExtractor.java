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
package de.dentrassi.pm.deb.aspect;

import java.nio.file.Path;
import java.util.Map;
import java.util.SortedMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.dentrassi.pm.aspect.ChannelAspect;
import de.dentrassi.pm.aspect.extract.Extractor;
import de.dentrassi.pm.utils.deb.Packages;

public class DebianExtractor implements Extractor
{
    private final ChannelAspect channelAspect;

    private final GsonBuilder builder;

    public DebianExtractor ( final ChannelAspect channelAspect )
    {
        this.channelAspect = channelAspect;
        this.builder = new GsonBuilder ();
    }

    @Override
    public ChannelAspect getAspect ()
    {
        return this.channelAspect;
    }

    @Override
    public void extractMetaData ( final Path file, final Map<String, String> metadata ) throws Exception
    {
        final SortedMap<String, String> controlFile;
        try
        {
            controlFile = Packages.parseControlFile ( file.toFile () );
        }
        catch ( final Exception e )
        {
            return;
        }

        if ( controlFile == null )
        {
            return;
        }

        metadata.put ( "package", controlFile.get ( "Package" ) );
        metadata.put ( "version", controlFile.get ( "Version" ) );
        metadata.put ( "maintainer", controlFile.get ( "Maintainer" ) );
        metadata.put ( "description", controlFile.get ( "Description" ) );
        metadata.put ( "shortDescription", makeShort ( controlFile.get ( "Description" ) ) );
        metadata.put ( "architecture", controlFile.get ( "Architecture" ) );
        metadata.put ( "section", controlFile.get ( "Section" ) );

        final Gson gson = this.builder.create ();

        metadata.put ( "control.json", gson.toJson ( new ControlInformation ( controlFile ) ) );
    }

    private String makeShort ( final String string )
    {
        if ( string == null )
        {
            return null;
        }

        final String toks[] = string.split ( "\n", 2 );
        if ( toks.length > 0 )
        {
            return toks[0];
        }
        return null;
    }
}
