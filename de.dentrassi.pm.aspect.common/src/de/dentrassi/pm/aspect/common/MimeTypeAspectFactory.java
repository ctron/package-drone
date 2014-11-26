/*******************************************************************************
 * Copyright (c) 2014 Jens Reimann.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jens Reimann - initial API and implementation
 *******************************************************************************/
package de.dentrassi.pm.aspect.common;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import de.dentrassi.pm.aspect.ChannelAspect;
import de.dentrassi.pm.aspect.ChannelAspectFactory;
import de.dentrassi.pm.aspect.extract.Extractor;

public class MimeTypeAspectFactory implements ChannelAspectFactory
{
    public static final String ID = "mime";

    private static class ChannelAspectImpl implements ChannelAspect
    {
        @Override
        public Extractor getExtractor ()
        {
            return new Extractor () {

                @Override
                public ChannelAspect getAspect ()
                {
                    return ChannelAspectImpl.this;
                }

                @Override
                public void extractMetaData ( final Path file, final Map<String, String> metadata ) throws Exception
                {
                    final String type = Files.probeContentType ( file );
                    if ( type != null )
                    {
                        metadata.put ( "type", type );
                    }
                }
            };
        }

        @Override
        public String getId ()
        {
            return ID;
        }

    }

    @Override
    public ChannelAspect createAspect ()
    {
        return new ChannelAspectImpl ();
    }

}
