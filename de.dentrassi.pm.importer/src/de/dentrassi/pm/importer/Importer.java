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

public interface Importer
{
    public static final String IMPORTER_ID = "package.drone.importer.id";

    /**
     * Import one or more artifacts into a channel
     *
     * @param channelId
     *            the channel to import to
     * @param properties
     *            the properties for the import
     */
    public void importForChannel ( String channelId, ImporterConfiguration configuration );

    /**
     * Import one ore more artifacts as children of another artifact
     *
     * @param parentArtifactId
     *            the artifact which should be used as parent
     * @param properties
     *            the properties for the import
     */
    public void importForArtifact ( String parentArtifactId, ImporterConfiguration configuration );

    public ImporterDescription getDescription ();
}
