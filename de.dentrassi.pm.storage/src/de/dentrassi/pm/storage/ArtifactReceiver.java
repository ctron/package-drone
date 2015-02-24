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
package de.dentrassi.pm.storage;

import java.io.InputStream;

import de.dentrassi.pm.common.SimpleArtifactInformation;

@FunctionalInterface
public interface ArtifactReceiver
{
    public void receive ( SimpleArtifactInformation information, InputStream stream ) throws Exception;
}
