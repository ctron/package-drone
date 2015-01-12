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
package de.dentrassi.pm.r5.handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.dentrassi.pm.common.servlet.Handler;
import de.dentrassi.pm.storage.Artifact;
import de.dentrassi.pm.storage.service.util.DownloadHelper;

public class DownloadHandler implements Handler
{

    private final Artifact artifact;

    public DownloadHandler ( final Artifact artifact )
    {
        this.artifact = artifact;
    }

    @Override
    public void prepare () throws Exception
    {
    }

    @Override
    public void process ( final HttpServletRequest req, final HttpServletResponse resp ) throws Exception
    {
        DownloadHelper.streamArtifact ( resp, this.artifact, "application/vnd.osgi.bundle", true );
    }

}
