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
package de.dentrassi.pm.p2.aspect;

import de.dentrassi.pm.aspect.ChannelAspect;
import de.dentrassi.pm.aspect.ChannelAspectFactory;
import de.dentrassi.pm.aspect.aggregate.ChannelAggregator;
import de.dentrassi.pm.aspect.extract.Extractor;
import de.dentrassi.pm.p2.internal.aspect.ExtractorImpl;
import de.dentrassi.pm.p2.internal.aspect.P2RepoChannelAggregator;

public class P2RepositoryAspect implements ChannelAspectFactory
{
    public static final String ID = "p2.repo";

    @Override
    public ChannelAspect createAspect ()
    {
        return new ChannelAspect () {

            @Override
            public String getId ()
            {
                return ID;
            }

            @Override
            public Extractor getExtractor ()
            {
                return new ExtractorImpl ( this );
            }

            @Override
            public ChannelAggregator getChannelAggregator ()
            {
                return new P2RepoChannelAggregator ();
            }
        };
    }

}
