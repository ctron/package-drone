package de.dentrassi.pm.storage.channel.provider;

import java.io.InputStream;
import java.util.Map;
import java.util.Set;

import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.storage.channel.ArtifactInformation;
import de.dentrassi.pm.storage.channel.ChannelDetails;

public interface ModifyContext extends AccessContext
{
    public void setDetails ( ChannelDetails details );

    public void applyMetaData ( Map<MetaKey, String> changes );

    public void applyMetaData ( String artifactId, Map<MetaKey, String> changes );

    public void lock ();

    public void unlock ();

    public ArtifactInformation createArtifact ( InputStream source, String name, Map<MetaKey, String> providedMetaData );

    public ArtifactInformation createArtifact ( String parentId, InputStream source, String name, Map<MetaKey, String> providedMetaData );

    public ArtifactInformation createGeneratorArtifact ( String generatorId, InputStream source, String name, Map<MetaKey, String> providedMetaData );

    public boolean deleteArtifact ( String artifactId );

    public void clear ();

    public void addAspects ( Set<String> aspectIds );

    public void removeAspects ( Set<String> aspectIds );

    public void refreshAspects ( Set<String> aspectIds );

    public void regenerate ( String artifactId );
}
