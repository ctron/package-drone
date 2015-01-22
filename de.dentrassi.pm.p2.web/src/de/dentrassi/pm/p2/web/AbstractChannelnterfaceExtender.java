package de.dentrassi.pm.p2.web;

import java.util.List;

import de.dentrassi.pm.storage.Channel;
import de.dentrassi.pm.storage.web.InterfaceExtender;
import de.dentrassi.pm.storage.web.menu.MenuEntry;

public abstract class AbstractChannelnterfaceExtender implements InterfaceExtender
{
    @Override
    public List<MenuEntry> getActions ( final Object object )
    {
        if ( object instanceof Channel )
        {
            return getChannelAction ( (Channel)object );
        }
        return null;
    }

    protected abstract List<MenuEntry> getChannelAction ( Channel channel );
}
