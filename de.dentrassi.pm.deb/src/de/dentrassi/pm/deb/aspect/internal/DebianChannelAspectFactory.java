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
package de.dentrassi.pm.deb.aspect.internal;

import de.dentrassi.pm.aspect.ChannelAspect;
import de.dentrassi.pm.aspect.ChannelAspectFactory;
import de.dentrassi.pm.aspect.extract.Extractor;

public class DebianChannelAspectFactory implements ChannelAspectFactory
{
    public static final String ID = "deb";

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
                return new DebianExtractor ();
            }
        };
    }

}
