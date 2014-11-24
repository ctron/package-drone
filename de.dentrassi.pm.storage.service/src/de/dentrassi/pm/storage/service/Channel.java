package de.dentrassi.pm.storage.service;

import java.util.List;
import java.util.Set;

import de.dentrassi.pm.meta.ChannelAspectInformation;

public interface Channel
{
    public String getId ();

    public Set<Artifact> getArtifacts ();

    public List<ChannelAspectInformation> getAspects ();
}
