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
package de.dentrassi.pm.importer;

public interface ImportContext extends ImportSubContext
{
    @FunctionalInterface
    public interface CleanupTask
    {
        public void cleanup () throws Exception;
    }

    public void addCleanupTask ( CleanupTask cleanup );
}
