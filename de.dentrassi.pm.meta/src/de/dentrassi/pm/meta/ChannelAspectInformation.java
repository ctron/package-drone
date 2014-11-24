package de.dentrassi.pm.meta;

public class ChannelAspectInformation
{
    private final String factoryId;

    private final String description;

    private final String label;

    private final boolean resolved;

    public ChannelAspectInformation ( final String factoryId, final String label, final String description )
    {
        this.factoryId = factoryId;
        this.label = label;
        this.description = description;
        this.resolved = true;
    }

    public ChannelAspectInformation ( final String factoryId, final String label, final String description, final boolean resolved )
    {
        this.factoryId = factoryId;
        this.label = label;
        this.description = description;
        this.resolved = resolved;
    }

    public String getDescription ()
    {
        return this.description;
    }

    public String getLabel ()
    {
        return this.label == null ? this.factoryId : this.label;
    }

    public String getFactoryId ()
    {
        return this.factoryId;
    }

    public boolean isResolved ()
    {
        return this.resolved;
    }

}
