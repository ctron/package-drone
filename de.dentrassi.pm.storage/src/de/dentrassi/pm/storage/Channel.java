/*******************************************************************************
 * Copyright (c) 2014, 2015 Jens Reimann.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jens Reimann - initial API and implementation
 *******************************************************************************/
package de.dentrassi.pm.storage;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import de.dentrassi.pm.common.ChannelAspectInformation;
import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.common.SimpleArtifactInformation;

public interface Channel
{
    public String getId ();

    public String getName ();

    public Set<Artifact> getArtifacts ();

    public List<ChannelAspectInformation> getAspects ();

    public boolean hasAspect ( String id );

    public Artifact createArtifact ( String name, InputStream stream, Map<MetaKey, String> providedMetaData );

    public Collection<Artifact> findByName ( String artifactName );

    public Set<SimpleArtifactInformation> getSimpleArtifacts ();

    public SortedMap<MetaKey, String> getMetaData ();

    public SortedMap<MetaKey, String> getProvidedMetaData ();

    public void applyMetaData ( Map<MetaKey, String> metadata );

    public Collection<DeployKey> getAllDeployKeys ();

    public Collection<DeployGroup> getDeployGroups ();

    public void addDeployGroup ( String groupId );

    public void removeDeployGroup ( String groupId );
}
