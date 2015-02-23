package de.dentrassi.pm.storage.jpa;

import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2015-02-23T11:22:30.867+0100")
@StaticMetamodel(DeployKeyEntity.class)
public class DeployKeyEntity_ {
	public static volatile SingularAttribute<DeployKeyEntity, String> id;
	public static volatile SingularAttribute<DeployKeyEntity, String> name;
	public static volatile SingularAttribute<DeployKeyEntity, String> keyData;
	public static volatile SingularAttribute<DeployKeyEntity, Date> creationTimestamp;
	public static volatile SingularAttribute<DeployKeyEntity, DeployGroupEntity> group;
}
