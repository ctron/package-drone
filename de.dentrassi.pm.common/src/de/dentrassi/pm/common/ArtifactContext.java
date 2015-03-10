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
package de.dentrassi.pm.common;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Map;

public interface ArtifactContext
{
    public ArtifactInformation getArtifactInformation ();

    public Path getFile ();

    /**
     * Create a new virtual artifact in the current context
     * 
     * @param name
     *            the name of the artifact
     * @param stream
     *            the input stream, has to be closed by the caller
     * @param providedMetaData
     *            the provided meta data, may be <code>null</code>
     */
    public void createVirtualArtifact ( String name, InputStream stream, Map<MetaKey, String> providedMetaData );

    public ArtifactInformation getOtherArtifactInformation ( String artifactId );
}
