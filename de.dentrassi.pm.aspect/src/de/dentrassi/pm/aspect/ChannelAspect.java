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
package de.dentrassi.pm.aspect;

import de.dentrassi.pm.aspect.listener.ChannelListener;
import de.dentrassi.pm.meta.extract.Extractor;

public interface ChannelAspect
{
    /**
     * Get the factory id
     *
     * @return the factory id
     */
    public String getId ();

    /**
     * @return an extractor or <code>null</code>
     */
    public default Extractor getExtractor ()
    {
        return null;
    }

    /**
     * @return a channel listener or <code>nulll</code>
     */
    public default ChannelListener getChannelListener ()
    {
        return null;
    }

}
