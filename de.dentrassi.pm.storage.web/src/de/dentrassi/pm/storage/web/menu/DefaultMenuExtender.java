package de.dentrassi.pm.storage.web.menu;

import java.util.LinkedList;
import java.util.List;

import de.dentrassi.pm.storage.web.menu.MenuManager.MenuEntry;

public class DefaultMenuExtender implements MenuExtender
{

    private final List<MenuEntry> entries = new LinkedList<> ();

    public DefaultMenuExtender ()
    {
    }

    @Override
    public List<MenuEntry> getEntries ()
    {
        return this.entries;
    }

    public void addEntry ( final String target, final String label, final int order )
    {
        this.entries.add ( new MenuEntry ( target, label, order ) );
    }

}
