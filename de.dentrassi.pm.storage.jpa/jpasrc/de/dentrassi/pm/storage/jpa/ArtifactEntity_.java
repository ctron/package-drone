package de.dentrassi.pm.storage.jpa;

import java.util.Date;

import javax.annotation.Generated;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated ( value = "Dali", date = "2015-05-05T10:08:23.533+0200" )
@StaticMetamodel ( ArtifactEntity.class )
public class ArtifactEntity_
{
    public static volatile SingularAttribute<ArtifactEntity, String> id;

    public static volatile SingularAttribute<ArtifactEntity, ChannelEntity> channel;

    public static volatile SingularAttribute<ArtifactEntity, String> name;

    public static volatile SingularAttribute<ArtifactEntity, Long> size;

    public static volatile SingularAttribute<ArtifactEntity, Date> creationTimestamp;

    public static volatile CollectionAttribute<ArtifactEntity, ExtractedArtifactPropertyEntity> extractedProperties;

    public static volatile CollectionAttribute<ArtifactEntity, ProvidedArtifactPropertyEntity> providedProperties;

    public static volatile CollectionAttribute<ArtifactEntity, ChildArtifactEntity> childArtifacts;

    public static volatile SingularAttribute<ArtifactEntity, Long> aggregatedNumberOfWarnings;

    public static volatile SingularAttribute<ArtifactEntity, Long> aggregatedNumberOfErrors;
}
