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
package de.dentrassi.pm.importer.http;

import de.dentrassi.osgi.web.LinkTarget;
import de.dentrassi.pm.importer.Importer;
import de.dentrassi.pm.importer.ImporterConfiguration;
import de.dentrassi.pm.importer.ImporterDescription;
import de.dentrassi.pm.importer.SimpleImporterDescription;

public class HttpImporter implements Importer
{
    public static final String ID = "http";

    private static final SimpleImporterDescription DESCRIPTION;

    static
    {
        DESCRIPTION = new SimpleImporterDescription ();
        DESCRIPTION.setId ( ID );
        DESCRIPTION.setLabel ( "HTTP Importer" );
        DESCRIPTION.setDescription ( "Import artifacts by downloading the provided URL" );
        DESCRIPTION.setStartTarget ( new LinkTarget ( "/import/{token}/http/start" ) );
    }

    @Override
    public void importForChannel ( final String channelId, final ImporterConfiguration configuration )
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void importForArtifact ( final String parentArtifactId, final ImporterConfiguration configuration )
    {
        // TODO Auto-generated method stub

    }

    @Override
    public ImporterDescription getDescription ()
    {
        return DESCRIPTION;
    }

}
