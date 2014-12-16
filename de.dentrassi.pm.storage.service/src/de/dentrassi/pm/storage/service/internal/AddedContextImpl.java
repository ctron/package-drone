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

import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

import de.dentrassi.pm.aspect.listener.AddedContext;
import de.dentrassi.pm.common.ArtifactInformation;
import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.storage.StorageAccessor;

public class AddedContextImpl implements AddedContext
{

    private final ArtifactInformation artifact;

    private final Map<MetaKey, String> metadata;

    private final Path file;

    private final StorageAccessor storage;

    public AddedContextImpl ( final ArtifactInformation artifact, final Map<MetaKey, String> metadata, final Path file, final StorageAccessor storage )
    {
        this.artifact = artifact;
        this.metadata = Collections.unmodifiableMap ( metadata );
        this.file = file;
        this.storage = storage;
    }

    @Override
    public String getChannelId ()
    {
        return this.artifact.getChannelId ();
    }

    @Override
    public String getName ()
    {
        return this.artifact.getName ();
    }

    @Override
    public Path getFile ()
    {
        return this.file;
    }

    @Override
    public String getId ()
    {
        return this.artifact.getId ();
    }

    @Override
    public Map<MetaKey, String> getMetaData ()
    {
        return this.metadata;
    }

    @Override
    public StorageAccessor getStorage ()
    {
        return this.storage;
    }

}
