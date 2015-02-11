package de.dentrassi.pm.maven.test;

import java.util.Collections;
import java.util.Date;

import org.junit.Test;

import de.dentrassi.pm.common.ArtifactInformation;
import de.dentrassi.pm.maven.ChannelData;
import de.dentrassi.pm.maven.MavenInformation;

public class ChannelDataTest
{
    @Test
    public void testSerialize ()
    {
        final ChannelData cd = new ChannelData ();

        final MavenInformation info = new MavenInformation ();

        info.setGroupId ( "a.b.c" );
        info.setArtifactId ( "d" );
        info.setVersion ( "v" );

        final ArtifactInformation art = new ArtifactInformation ( "id", null, 0L, "name", "channelId", new Date (), Collections.emptySet (), Collections.emptySortedMap (), Collections.emptySortedSet () );
        cd.add ( info, art );

        System.out.println ( " == JSON == " );
        System.out.println ( cd );
    }

    @Test
    public void testFull ()
    {
        ChannelData cd = new ChannelData ();

        final MavenInformation info = new MavenInformation ();

        info.setGroupId ( "a.b.c" );
        info.setArtifactId ( "d" );
        info.setVersion ( "v" );

        final ArtifactInformation art = new ArtifactInformation ( "id", null, 0L, "name", "channelId", new Date (), Collections.emptySet (), Collections.emptySortedMap (), Collections.emptySortedSet () );
        cd.add ( info, art );

        cd = ChannelData.fromJson ( cd.toJson () );

        System.out.println ( " == FULL == " );
        System.out.println ( cd );
    }
}
