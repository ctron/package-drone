/*******************************************************************************
 * Copyright (c) 2014 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package de.dentrassi.pm.common;

public class ChannelAspectInformation
{
    private final String factoryId;

    private final String description;

    private final String label;

    private final boolean resolved;

    private ChannelAspectInformation ( final String factoryId )
    {
        this.factoryId = factoryId;
        this.resolved = false;

        this.label = null;
        this.description = null;
    }

    public ChannelAspectInformation ( final String factoryId, final String label, final String description )
    {
        this.factoryId = factoryId;
        this.label = label;
        this.description = description;
        this.resolved = true;
    }

    public ChannelAspectInformation ( final String factoryId, final String label, final String description, final boolean resolved )
    {
        this.factoryId = factoryId;
        this.label = label;
        this.description = description;
        this.resolved = resolved;
    }

    public String getDescription ()
    {
        return this.description;
    }

    public String getLabel ()
    {
        return this.label == null ? this.factoryId : this.label;
    }

    public String getFactoryId ()
    {
        return this.factoryId;
    }

    public boolean isResolved ()
    {
        return this.resolved;
    }

    /**
     * Create an unresolved information instance
     * 
     * @param factoryId
     *            the factory id
     * @return a new information instance
     */
    public static ChannelAspectInformation unresolved ( final String factoryId )
    {
        return new ChannelAspectInformation ( factoryId );
    }

}
