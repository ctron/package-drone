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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;

import de.dentrassi.pm.common.ArtifactInformation;
import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.common.utils.ThrowingConsumer;
import de.dentrassi.pm.storage.Artifact;
import de.dentrassi.pm.storage.ArtifactReceiver;
import de.dentrassi.pm.storage.Channel;
import de.dentrassi.pm.storage.ValidationMessage;

public class MockArtifact implements Artifact
{

    private final String id;

    private final ArtifactInformation information;

    public MockArtifact ( final String id, final String name, final SortedMap<MetaKey, String> metaData )
    {
        this.id = id;
        this.information = new ArtifactInformation ( id, null, 0, name, "", new Date (), 0L, 0L, new HashSet<> (), metaData, new TreeSet<> () );
    }

    @Override
    public int compareTo ( final Artifact o )
    {
        return this.id.compareTo ( o.getId () );
    }

    @Override
    public List<ValidationMessage> getValidationMessages ()
    {
        return Collections.emptyList ();
    }

    @Override
    public Channel getChannel ()
    {
        return null;
    }

    @Override
    public String getId ()
    {
        return this.id;
    }

    @Override
    public boolean streamData ( final ArtifactReceiver receiver )
    {
        try
        {
            receiver.receive ( getInformation (), new ByteArrayInputStream ( new byte[0] ) );
            return true;
        }
        catch ( final Exception e )
        {
            throw new RuntimeException ( e );
        }
    }

    @Override
    public boolean streamData ( final ThrowingConsumer<InputStream> receiver )
    {
        return streamData ( ( info, stream ) -> receiver.accept ( stream ) );
    }

    @Override
    public void applyMetaData ( final Map<MetaKey, String> metadata )
    {
        // no op
    }

    @Override
    public Artifact getParent ()
    {
        return null;
    }

    @Override
    public ArtifactInformation getInformation ()
    {
        return this.information;
    }

    @Override
    public Artifact attachArtifact ( final String name, final InputStream stream, final Map<MetaKey, String> providedMetaData )
    {
        // no op
        return null;
    }

    public static MockArtifact maven ( final String groupId, final String artifactId, String version, final String extension, final String snapshotSuffix )
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
        return new MockArtifact ( UUID.randomUUID ().toString (), name, metaData );
    }

    public static class Builder
    {
        private final SortedMap<MetaKey, String> metaData = new TreeMap<> ();

        private final String name;

        public Builder ( final String name )
        {
            this.name = name;
        }

        public void add ( final String ns, final String key, final String value )
        {
            this.metaData.put ( new MetaKey ( ns, key ), value );
        }

        public Artifact build ()
        {
            return new MockArtifact ( UUID.randomUUID ().toString (), this.name, this.metaData );
        }
    }

}
