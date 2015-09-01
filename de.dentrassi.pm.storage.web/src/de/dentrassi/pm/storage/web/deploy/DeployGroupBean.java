package de.dentrassi.pm.storage.web.deploy;

import de.dentrassi.pm.storage.channel.deploy.DeployGroup;

public class DeployGroupBean
{
    private String name;

    public void setName ( final String name )
    {
        this.name = name;
    }

    public String getName ()
    {
        return this.name;
    }

    public static DeployGroupBean fromGroup ( final DeployGroup group )
    {
        final DeployGroupBean result = new DeployGroupBean ();
        result.setName ( group.getName () );
        return result;
    }

}
