package de.dentrassi.pm.storage.service;

import java.util.Map;

public interface Artifact extends Comparable<Artifact>
{
    public Channel getChannel ();

    public String getId ();

    public long getSize ();

    public String getName ();

    public void streamData ( ArtifactReceiver receiver );

    public Map<MetaKey, String> getMetaData ();

    public void applyMetaData ( Map<MetaKey, String> metadata );
}
