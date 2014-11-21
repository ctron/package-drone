package de.dentrassi.pm.storage.service;

public interface Artifact extends Comparable<Artifact>
{
    public Channel getChannel ();

    public String getId ();

    public long getSize ();

    public String getName ();

    public void streamData ( ArtifactReceiver receiver );
}
