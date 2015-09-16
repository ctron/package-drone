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
package de.dentrassi.pm.unzip;

import static java.time.Instant.now;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;

import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;

import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.storage.channel.ArtifactInformation;

public class MockArtifact
{

    private MockArtifact ()
    {
    }

    public static ArtifactInformation maven ( final String channelId, final String groupId, final String artifactId, String version, final String extension, final String snapshotSuffix )
    {
        final SortedMap<MetaKey, String> metaData = new TreeMap<> ();

        metaData.put ( new MetaKey ( "mvn", "groupId" ), groupId );
        metaData.put ( new MetaKey ( "mvn", "artifactId" ), artifactId );

        metaData.put ( new MetaKey ( "mvn", "extension" ), extension );

        String snapshotVersion = null;
        if ( snapshotSuffix != null )
        {
            snapshotVersion = version + "-" + snapshotSuffix;
            metaData.put ( new MetaKey ( "mvn", "snapshotVersion" ), snapshotVersion );
            version = version + "-SNAPSHOT";
        }

        metaData.put ( new MetaKey ( "mvn", "version" ), version );

        final String name = String.format ( "%s-%s.%s", artifactId, snapshotSuffix != null ? snapshotVersion : version, extension );

        final String id = UUID.randomUUID ().toString ();

        return new ArtifactInformation ( id, null, emptySet (), name, 0L, now (), singleton ( "stored" ), emptyList (), metaData, emptyMap (), null );
    }

}
