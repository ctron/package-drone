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
package de.dentrassi.pm.maven.internal;

import de.dentrassi.pm.aspect.ChannelAspect;
import de.dentrassi.pm.aspect.ChannelAspectFactory;
import de.dentrassi.pm.aspect.virtual.Virtualizer;

public class MavenPomExtractorFactory implements ChannelAspectFactory
{
    public static final String ID = "mvn.pom";

    @Override
    public ChannelAspect createAspect ()
    {
        class MavenPomExtractorAspect implements ChannelAspect
        {
            @Override
            public String getId ()
            {
                return ID;
            }

            @Override
            public Virtualizer getArtifactVirtualizer ()
            {
                return new MavenPomVirtualizer ();
            }
        };

        return new MavenPomExtractorAspect ();
    }

}
