package de.dentrassi.pm.storage.channel.apm;

import java.util.Collections;
import java.util.Date;
import java.util.Map;

import de.dentrassi.pm.storage.channel.ArtifactInformation;

public class ArtifactModel
{
    private String name;

    private long size;

    private Date date;

    public ArtifactModel ()
    {
    }

    public ArtifactModel ( final ArtifactModel other )
    {
        this.name = other.name;
        this.size = other.size;
        this.date = other.date;
    }

    public void setName ( final String name )
    {
        this.name = name;
    }

    public String getName ()
    {
        return this.name;
    }

    public void setSize ( final long size )
    {
        this.size = size;
    }

    public long getSize ()
    {
        return this.size;
    }

    public void setDate ( final Date date )
    {
        this.date = date;
    }

    public Date getDate ()
    {
        return this.date;
    }

    public static ArtifactInformation toInformation ( final String id, final ArtifactModel model )
    {
        return new ArtifactInformation ( id, model.getName (), model.getSize (), model.getDate ().toInstant (), Collections.emptySet () );
    }

    public static ArtifactInformation toInformation ( final Map.Entry<String, ArtifactModel> entry )
    {
        return toInformation ( entry.getKey (), entry.getValue () );
    }
}
