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

import java.util.Collections;
import java.util.Map;

import de.dentrassi.pm.aspect.listener.RemovedContext;
import de.dentrassi.pm.storage.MetaKey;

public class RemovedContextImpl implements RemovedContext
{
    private final String id;

    private final String name;

    private final Map<MetaKey, String> metadata;

    public RemovedContextImpl ( final String id, final String name, final Map<MetaKey, String> metadata )
    {
        this.id = id;
        this.name = name;
        this.metadata = Collections.unmodifiableMap ( metadata );
    }

    @Override
    public String getName ()
    {
        return this.name;
    }

    @Override
    public String getId ()
    {
        return this.id;
    }

    @Override
    public Map<MetaKey, String> getMetaData ()
    {
        return this.metadata;
    }

}
