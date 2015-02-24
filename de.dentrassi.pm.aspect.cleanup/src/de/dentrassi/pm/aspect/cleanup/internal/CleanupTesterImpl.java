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
package de.dentrassi.pm.aspect.cleanup.internal;

import java.util.Collection;
import java.util.List;
import java.util.SortedMap;

import de.dentrassi.pm.aspect.cleanup.CleanupConfiguration;
import de.dentrassi.pm.aspect.cleanup.CleanupTester;
import de.dentrassi.pm.aspect.cleanup.ResultEntry;
import de.dentrassi.pm.aspect.cleanup.ResultKey;
import de.dentrassi.pm.common.ArtifactInformation;

public class CleanupTesterImpl implements CleanupTester
{

    @Override
    public SortedMap<ResultKey, List<ResultEntry>> testCleanup ( final Collection<ArtifactInformation> artifacts, final CleanupConfiguration configuration )
    {
        return CleanupListener.process ( configuration, CleanupListener.aggregate ( configuration.getAggregator (), configuration.getSorter (), artifacts ) );
    }

}
