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

import de.dentrassi.osgi.web.LinkTarget;
import de.dentrassi.pm.common.ArtifactInformation;
import de.dentrassi.pm.storage.GeneratorArtifact;

public class GeneratorArtifactImpl extends ArtifactImpl implements GeneratorArtifact
{
    private final LinkTarget editTarget;

    public GeneratorArtifactImpl ( final ChannelImpl channel, final String id, final ArtifactInformation information, final LinkTarget editTarget )
    {
        super ( channel, id, information );
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
