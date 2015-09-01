package de.dentrassi.pm.storage.channel.apm;

import java.nio.file.Path;

import de.dentrassi.pm.aspect.listener.PreAddContext;

public class PreAddContextImpl implements PreAddContext
{
    private final String name;

    private final Path file;

    private final boolean external;

    public PreAddContextImpl ( final String name, final Path file, final boolean external )
    {
        this.name = name;
        this.file = file;
        this.external = external;
    }

    private boolean veto;

    @Override
    public String getName ()
    {
        return this.name;
    }

    @Override
    public Path getFile ()
    {
        return this.file;
    }

    @Override
    public void vetoAdd ()
    {
        this.veto = true;
    }

    public boolean isVeto ()
    {
        return this.veto;
    }

    @Override
    public boolean isExternal ()
    {
        return this.external;
    }

}
