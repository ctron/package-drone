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
package de.dentrassi.pm.core;

import java.util.Map;

import de.dentrassi.pm.common.MetaKey;

public interface CoreService
{
    /**
     * Get a core property
     *
     * @param key
     *            the name of the property
     * @return the value, or <code>null</code> if the property was not found
     */
    public String getCoreProperty ( String key );

    /**
     * Get a core property, or a default value instead <br/>
     * If the property entry is found but has a value of <code>null</code> the
     * value <code>null</code> is still being returned. However
     * the method {@link #setCoreProperty(String, String)} will automatically
     * delete entries when the value <code>null</code> is set.
     *
     * @param key
     *            the name of the property
     * @param defaultValue
     *            the default value
     * @return the value or the default value in case the property entry was not
     *         found.
     */
    public String getCoreProperty ( String key, String defaultValue );

    public void setCoreProperty ( String key, String value );

    public Map<MetaKey, String> list ();

    public void setProperties ( Map<String, String> properties );
}
