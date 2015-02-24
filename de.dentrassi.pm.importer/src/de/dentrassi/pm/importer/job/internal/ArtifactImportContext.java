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
package de.dentrassi.pm.importer.job.internal;

import java.io.InputStream;
import java.util.Map;

import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.storage.Artifact;
import de.dentrassi.pm.storage.service.StorageService;

public class ArtifactImportContext extends AbstractImportContext
{
    private final Artifact artifact;

    public ArtifactImportContext ( final StorageService service, final String artifactId )
    {
        this.artifact = service.getArtifact ( artifactId );
        if ( this.artifact == null )
        {
            throw new IllegalArgumentException ( String.format ( "Artifact '%s' cannot be found", artifactId ) );
        }
    }

    @Override
    protected String getChannelId ()
    {
        return this.artifact.getChannel ().getId ();
    }

    @Override
    protected Artifact performRootImport ( final InputStream stream, final String name, final Map<MetaKey, String> providedMetaData )
    {
        return this.artifact.attachArtifact ( name, stream, providedMetaData );
    }
}
