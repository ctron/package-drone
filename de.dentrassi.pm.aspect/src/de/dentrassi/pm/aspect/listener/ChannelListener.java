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
package de.dentrassi.pm.aspect.listener;

public interface ChannelListener
{
    public void artifactPreAdd ( PreAddContext context );

    public void artifactAdded ( AddedContext context );

    public void artifactRemoved ( RemovedContext context );
}
