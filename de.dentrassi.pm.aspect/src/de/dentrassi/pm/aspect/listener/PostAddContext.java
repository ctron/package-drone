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
package de.dentrassi.pm.aspect.listener;

import java.util.Collection;
import java.util.Map;

import de.dentrassi.pm.common.ArtifactInformation;
import de.dentrassi.pm.common.MetaKey;

public interface PostAddContext
{
    public Collection<ArtifactInformation> getChannelArtifacts ();

    public Map<MetaKey, String> getChannelMetaData ();

    public void deleteArtifact ( String artifactId );
}
