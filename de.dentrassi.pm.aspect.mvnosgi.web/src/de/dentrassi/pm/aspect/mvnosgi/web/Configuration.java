package de.dentrassi.pm.aspect.mvnosgi.web;

import de.dentrassi.pm.common.MetaKeyBinding;

public class Configuration
{
    @MetaKeyBinding ( namespace = "mvnosgi", key = "groupId" )
    private String groupId;

    public void setGroupId ( final String groupId )
    {
        this.groupId = groupId;
    }

    public String getGroupId ()
    {
        return this.groupId;
    }
}
