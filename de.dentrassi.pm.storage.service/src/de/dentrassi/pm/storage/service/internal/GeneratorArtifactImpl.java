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
package de.dentrassi.pm.storage.service.internal;

import java.util.Date;
import java.util.Map;

import de.dentrassi.osgi.web.LinkTarget;
import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.storage.GeneratorArtifact;

public class GeneratorArtifactImpl extends ArtifactImpl implements GeneratorArtifact
{
    private final LinkTarget editTarget;

    public GeneratorArtifactImpl ( final ChannelImpl channel, final String id, final String name, final long size, final Map<MetaKey, String> metaData, final Date creationTimestamp, final LinkTarget editTarget )
    {
        super ( channel, id, null, name, size, metaData, creationTimestamp, false, true, false );
        this.editTarget = editTarget;
    }

    @Override
    public LinkTarget getEditTarget ()
    {
        return this.editTarget;
    }

    @Override
    public void generate ()
    {
        this.channel.generate ( this.id );
    }

}
