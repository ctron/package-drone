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
package de.dentrassi.pm.core;

import java.util.Arrays;
import java.util.Collection;
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
     * Get multiple core properties
     * <p>
     * <em>Note:</em> The keys in the requested key collection must be unique.
     * The result map will contain keys exactly once.
     * </p>
     *
     * @param keys
     *            the collection of unique keys to read
     * @return the map, containing a mapping from the key to the value (may be
     *         <code>null</code>). Never returns null.
     */
    public Map<String, String> getCoreProperties ( Collection<String> keys );

/**
     * Get multiple core properties
     * <p>
     * The method is a convenience method to
     * {@link #getCoreProperties(Collection)} and will behave as is if {@link
     * <code>getCoreProperties(Arrays.asList(keys))</code> would have been called.
     * </p>
     *
     * @param keys
     *            the array of unique keys to read
     * @return the map, containing a mapping from the key to the value (may be
     *         <code>null</code>). Never returns null.
     */
    public default Map<String, String> getCoreProperties ( final String... keys )
    {
        return getCoreProperties ( Arrays.asList ( keys ) );
    }

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
