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
package de.dentrassi.pm.generator;

import de.dentrassi.osgi.web.LinkTarget;
import de.dentrassi.pm.storage.channel.ChannelArtifactInformation;

public interface ArtifactGenerator
{
    public static final String GENERATOR_ID_PROPERTY = "pm.generator.id";

    public void generate ( GenerationContext context ) throws Exception;

    public boolean shouldRegenerate ( Object event );

    public LinkTarget getAddTarget ();

    public default LinkTarget getEditTarget ( final ChannelArtifactInformation artifact )
    {
        return null;
    }
}
