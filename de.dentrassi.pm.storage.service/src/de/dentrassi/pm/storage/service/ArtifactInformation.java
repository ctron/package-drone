package de.dentrassi.pm.storage.service;

public class ArtifactInformation
{
    private final long length;

    private final String name;

    public ArtifactInformation ( final long size, final String name )
    {
        this.length = size;
        this.name = name;
    }

    public long getLength ()
    {
        return this.length;
    }

    public String getName ()
    {
        return this.name;
    }
}
