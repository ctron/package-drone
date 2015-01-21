package de.dentrassi.pm.storage.web;

import java.util.List;

import de.dentrassi.pm.storage.web.menu.MenuEntry;

public interface InterfaceExtender
{
    public default List<MenuEntry> getActions ( final Object object )
    {
        return null;
    }

    public default List<MenuEntry> getMainMenuEntries ()
    {
        return null;
    }
}
