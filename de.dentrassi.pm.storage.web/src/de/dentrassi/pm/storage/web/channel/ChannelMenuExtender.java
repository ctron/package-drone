package de.dentrassi.pm.storage.web.channel;

import java.util.LinkedList;
import java.util.List;

import de.dentrassi.pm.storage.service.StorageService;
import de.dentrassi.pm.storage.web.menu.MenuExtender;
import de.dentrassi.pm.storage.web.menu.MenuManager.MenuEntry;

public class ChannelMenuExtender implements MenuExtender
{
    private final List<MenuEntry> entries = new LinkedList<> ();

    public void setService ( final StorageService service )
    {
        // dummy method
    }

    public ChannelMenuExtender ()
    {
        this.entries.add ( new MenuEntry ( "/channel", "Channels", 10 ) );
    }

    @Override
    public List<MenuEntry> getEntries ()
    {
        return this.entries;
    }

}
