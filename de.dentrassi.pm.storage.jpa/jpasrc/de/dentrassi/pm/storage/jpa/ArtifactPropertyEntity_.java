package de.dentrassi.pm.storage.jpa;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2014-11-26T12:19:29.334+0100")
@StaticMetamodel(ArtifactPropertyEntity.class)
public class ArtifactPropertyEntity_ {
	public static volatile SingularAttribute<ArtifactPropertyEntity, String> artifactId;
	public static volatile SingularAttribute<ArtifactPropertyEntity, String> namespace;
	public static volatile SingularAttribute<ArtifactPropertyEntity, String> key;
	public static volatile SingularAttribute<ArtifactPropertyEntity, String> value;
	public static volatile SingularAttribute<ArtifactPropertyEntity, ArtifactEntity> artifact;
}
