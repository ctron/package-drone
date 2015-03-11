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
package de.dentrassi.pm.p2.internal.aspect;

import java.io.IOException;
import java.io.OutputStream;

import de.dentrassi.pm.common.ArtifactInformation;

public interface Processor
{
    public void process ( ArtifactInformation artifact, ArtifactStreamer streamer ) throws Exception;

    public void write ( final OutputStream stream ) throws IOException;

    public default String getMimeType ()
    {
        return "application/octet-stream";
    }

    public default String getName ()
    {
        return getId ();
    }

    public String getId ();
}
