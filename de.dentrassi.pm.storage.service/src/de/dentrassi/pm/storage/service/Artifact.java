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
package de.dentrassi.pm.storage.service;

import java.util.Map;
import java.util.SortedMap;

public interface Artifact extends Comparable<Artifact>
{
    public Channel getChannel ();

    public String getId ();

    public long getSize ();

    public String getName ();

    public void streamData ( ArtifactReceiver receiver );

    public SortedMap<MetaKey, String> getMetaData ();

    public void applyMetaData ( Map<MetaKey, String> metadata );
}
