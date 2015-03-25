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
package de.dentrassi.pm.storage.service;

import java.io.File;
import java.io.IOException;

/**
 * An interface which hold methods for configuring a storage service, but which
 * should not be part of the storage service itself.
 * <p>
 * An implementation of {@link StorageService} is not required to implement this
 * interface
 * </p>
 */
public interface StorageServiceAdmin
{
    public void setBlobStoreLocation ( File location ) throws IOException;
}
