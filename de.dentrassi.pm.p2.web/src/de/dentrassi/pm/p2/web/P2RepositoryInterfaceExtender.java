package de.dentrassi.pm.p2.web;

import java.util.Collections;
import java.util.List;

import de.dentrassi.osgi.web.LinkTarget;
import de.dentrassi.pm.storage.Channel;
import de.dentrassi.pm.storage.web.Modifier;
import de.dentrassi.pm.storage.web.menu.MenuEntry;

public class P2RepositoryInterfaceExtender extends AbstractChannelnterfaceExtender
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

    @Override
    protected List<MenuEntry> getChannelAction ( final Channel channel )
    {
        return Collections.singletonList ( new MenuEntry ( "Adapters", 1000, "P2", 0, new LinkTarget ( "" ), Modifier.DEFAULT, false ) );
    }
}
