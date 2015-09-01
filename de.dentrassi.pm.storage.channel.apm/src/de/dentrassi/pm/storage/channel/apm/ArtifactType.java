package de.dentrassi.pm.storage.channel.apm;

public enum ArtifactType
{
    STORED ( "stored", true ),
    VIRTUAL ( "virtual", false );

    private String facetType;

    private boolean external;

    private ArtifactType ( final String facetType, final boolean external )
    {
        this.facetType = facetType;
        this.external = external;
    }

    public String getFacetType ()
    {
        return this.facetType;
    }

    public boolean isExternal ()
    {
        return this.external;
    }

}