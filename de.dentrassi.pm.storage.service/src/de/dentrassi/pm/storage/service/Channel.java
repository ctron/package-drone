package de.dentrassi.pm.storage.service;

import java.util.Set;

public interface Channel
{
    public String getId ();

    public Set<Artifact> getArtifacts ();
}
