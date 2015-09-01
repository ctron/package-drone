package de.dentrassi.pm.storage.web.deploy;

import de.dentrassi.pm.storage.channel.deploy.DeployKey;

public class DeployKeyBean
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

    public static DeployKeyBean fromKey ( final DeployKey key )
    {
        if ( key == null )
        {
            return null;
        }

        final DeployKeyBean result = new DeployKeyBean ();

        result.setName ( key.getName () );

        return result;
    }
}
