package de.dentrassi.pm.storage.channel.apm;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.common.utils.IOConsumer;
import de.dentrassi.pm.storage.channel.ArtifactInformation;

public interface AspectableContext
{
    public AspectMapModel getAspectModel ();

    public ArtifactInformation createPlainArtifact ( String parentArtifactId, InputStream source, String name, Map<MetaKey, String> providedMetaData, Set<String> facets, String virtualizerAspectId );

    public boolean deletePlainArtifact ( String artifactId );

    public boolean stream ( String artifactId, IOConsumer<InputStream> consumer ) throws IOException;

    public ArtifactInformation setExtractedMetaData ( String artifactId, Map<MetaKey, String> metaData );

    public Map<String, ArtifactInformation> getArtifacts ();

    public Map<MetaKey, String> getChannelProvidedMetaData ();
}
