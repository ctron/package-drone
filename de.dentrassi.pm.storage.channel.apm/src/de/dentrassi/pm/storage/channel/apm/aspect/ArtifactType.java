package de.dentrassi.pm.storage.channel.apm.aspect;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public enum ArtifactType
{
    STORED ( Arrays.asList ( "stored" ), true ),
    VIRTUAL ( Arrays.asList ( "virtual" ), false ),
    GENERATOR ( Arrays.asList ( "stored", "generator" ), true ),
    GENERATED ( Arrays.asList ( "generated" ), true );

    private Set<String> facetTypes;

    private boolean external;

    private ArtifactType ( final List<String> facetType, final boolean external )
    {
        this.facetTypes = Collections.unmodifiableSet ( new HashSet<> ( facetType ) );
        this.external = external;
    }

    public Set<String> getFacetTypes ()
    {
        return this.facetTypes;
    }

    public boolean isExternal ()
    {
        return this.external;
    }

}
