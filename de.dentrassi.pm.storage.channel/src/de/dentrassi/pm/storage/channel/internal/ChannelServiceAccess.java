package de.dentrassi.pm.storage.channel.internal;

import java.util.List;
import java.util.Map;
import java.util.Set;

import de.dentrassi.pm.storage.channel.deploy.DeployGroup;
import de.dentrassi.pm.storage.channel.deploy.DeployKey;

public interface ChannelServiceAccess
{
    public String mapToId ( String name );

    public String mapToName ( String id );

    public Map<String, String> getNameMap ();

    public List<DeployGroup> getDeployGroups ();

    public DeployGroup getDeployGroup ( String id );

    public DeployKey getDeployKey ( String keyId );

    /**
     * Get the map of channel ids to deploy groups
     * 
     * @return the channel to deploy group map
     */
    public Map<String, Set<String>> getDeployGroupMap ();
}
