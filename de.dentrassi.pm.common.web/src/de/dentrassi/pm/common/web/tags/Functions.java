/*******************************************************************************
 * Copyright (c) 2014, 2015 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package de.dentrassi.pm.common.web.tags;

import de.dentrassi.pm.common.web.Modifier;

public class Functions
{
    /**
     * Convert a modifier value to a bootstrap type modifier
     *
     * @param prefix
     *            an optional prefix to add
     * @param modifier
     *            the modifier to convert
     * @return the bootstrap type string, optionally with a prefix attached,
     *         never <code>null</code>
     */
    public static String modifier ( final String prefix, final Modifier modifier )
    {
        if ( modifier == null )
        {
            return "";
        }

        String value = null;
        switch ( modifier )
        {
            case DEFAULT:
                value = "default";
                break;
            case PRIMARY:
                value = "primary";
                break;
            case SUCCESS:
                value = "success";
                break;
            case INFO:
                value = "info";
                break;
            case WARNING:
                value = "warning";
                break;
            case DANGER:
                value = "danger";
                break;
            case LINK:
                value = "link";
                break;
        }

        if ( value != null && prefix != null )
        {
            return prefix + value;
        }
        else
        {
            return value != null ? value : "";
        }
    }
}
