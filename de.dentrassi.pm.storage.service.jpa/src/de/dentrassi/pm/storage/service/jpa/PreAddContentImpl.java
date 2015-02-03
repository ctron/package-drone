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
package de.dentrassi.pm.storage.service.jpa;

import java.nio.file.Path;

import de.dentrassi.pm.aspect.listener.PreAddContext;

public class PreAddContentImpl implements PreAddContext
{

    private final String name;

    private final Path file;

    private boolean veto;

    private final String channelId;

    public PreAddContentImpl ( final String name, final Path file, final String channelId )
    {
        this.name = name;
        this.file = file;
        this.channelId = channelId;
    }

    @Override
    public String getChannelId ()
    {
        return this.channelId;
    }

    @Override
    public String getName ()
    {
        return this.name;
    }

    @Override
    public Path getFile ()
    {
        return this.file;
    }

    @Override
    public void vetoAdd ()
    {
        this.veto = true;
    }

    public boolean isVeto ()
    {
        return this.veto;
    }

}
