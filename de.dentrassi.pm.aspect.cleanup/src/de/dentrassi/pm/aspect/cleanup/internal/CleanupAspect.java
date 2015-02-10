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
package de.dentrassi.pm.aspect.cleanup.internal;

import de.dentrassi.pm.aspect.ChannelAspect;
import de.dentrassi.pm.aspect.aggregate.ChannelAggregator;

public class CleanupAspect implements ChannelAspect
{
    public static final String ID = "cleanup";

    @Override
    public String getId ()
    {
        return ID;
    }

    @Override
    public ChannelAggregator getChannelAggregator ()
    {
        return new CleanupAggregator ();
    }
}
