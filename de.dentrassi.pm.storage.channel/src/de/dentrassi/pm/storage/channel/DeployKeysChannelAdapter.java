package de.dentrassi.pm.storage.channel;

import java.util.Set;

import de.dentrassi.pm.storage.channel.deploy.DeployGroup;

public interface DeployKeysChannelAdapter
{
    public Set<DeployGroup> getDeployGroups ();

    public void assignDeployGroup ( String groupId );

    public void unassignDeployGroup ( String groupId );
}
