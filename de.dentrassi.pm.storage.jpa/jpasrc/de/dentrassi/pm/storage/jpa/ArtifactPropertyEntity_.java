package de.dentrassi.pm.storage.jpa;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2015-02-16T16:15:35.197+0100")
@StaticMetamodel(ArtifactPropertyEntity.class)
public class ArtifactPropertyEntity_ {
	public static volatile SingularAttribute<ArtifactPropertyEntity, ArtifactEntity> artifact;
	public static volatile SingularAttribute<ArtifactPropertyEntity, String> namespace;
	public static volatile SingularAttribute<ArtifactPropertyEntity, String> key;
	public static volatile SingularAttribute<ArtifactPropertyEntity, String> value;
}
