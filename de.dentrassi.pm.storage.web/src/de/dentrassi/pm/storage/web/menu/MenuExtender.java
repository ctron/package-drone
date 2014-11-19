package de.dentrassi.pm.storage.web.menu;

import java.util.List;

import de.dentrassi.pm.storage.web.menu.MenuManager.MenuEntry;

public interface MenuExtender
{
    public List<MenuEntry> getEntries ();
}
