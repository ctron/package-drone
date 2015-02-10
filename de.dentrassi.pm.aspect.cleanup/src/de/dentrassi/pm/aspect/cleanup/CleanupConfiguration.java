/*******************************************************************************
 * Copyright (c) 2015 Jens Reimann.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jens Reimann - initial API and implementation
 *******************************************************************************/
package de.dentrassi.pm.aspect.cleanup;

import de.dentrassi.pm.aspect.cleanup.internal.CleanupAspect;
import de.dentrassi.pm.common.MetaKeyBinding;

public class CleanupConfiguration
{
    @MetaKeyBinding ( namespace = CleanupAspect.ID, key = "number-of-versions" )
    private int numberOfVersions = 3;

    @MetaKeyBinding ( namespace = CleanupAspect.ID, key = "sorter" )
    private Sorter sorter;

    @MetaKeyBinding ( namespace = CleanupAspect.ID, key = "aggregator" )
    private Aggregator aggregator;

    public Aggregator getAggregator ()
    {
        return this.aggregator;
    }

    public void setAggregator ( final Aggregator aggregator )
    {
        this.aggregator = aggregator;
    }

    public void setSorter ( final Sorter sorter )
    {
        this.sorter = sorter;
    }

    public Sorter getSorter ()
    {
        return this.sorter;
    }

    public void setNumberOfVersions ( final int numberOfVersions )
    {
        this.numberOfVersions = numberOfVersions;
    }

    public int getNumberOfVersions ()
    {
        return this.numberOfVersions;
    }
}
