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
package de.dentrassi.pm.common;

import java.util.Map;

public final class MetaKeys
{
    private MetaKeys ()
    {
    }

    public static String getString ( final Map<MetaKey, String> metadata, final String ns, final String key )
    {
        if ( metadata == null )
        {
            return null;
        }
        return metadata.get ( new MetaKey ( ns, key ) );
    }
}
