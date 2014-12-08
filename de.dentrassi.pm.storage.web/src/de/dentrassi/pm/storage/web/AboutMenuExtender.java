package de.dentrassi.pm.storage.web;

import java.util.LinkedList;
import java.util.List;

import de.dentrassi.pm.storage.web.menu.MenuExtender;
import de.dentrassi.pm.storage.web.menu.MenuManager.MenuEntry;

public class AboutMenuExtender implements MenuExtender
{
    private final List<MenuEntry> entries = new LinkedList<> ();

    public AboutMenuExtender ()
    {
        this.entries.add ( new MenuEntry ( "https://github.com/ctron/package-drone/wiki", "About", Integer.MAX_VALUE, true ) );
    }

    @Override
    public List<MenuEntry> getEntries ()
    {
        return this.entries;
    }
}
