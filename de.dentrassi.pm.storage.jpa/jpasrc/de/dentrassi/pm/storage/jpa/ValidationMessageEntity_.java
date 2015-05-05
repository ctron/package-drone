package de.dentrassi.pm.storage.jpa;

import javax.annotation.Generated;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2015-05-04T14:48:34.925+0200")
@StaticMetamodel(ValidationMessageEntity.class)
public class ValidationMessageEntity_ {
	public static volatile SingularAttribute<ValidationMessageEntity, String> id;
	public static volatile SingularAttribute<ValidationMessageEntity, ChannelEntity> channel;
	public static volatile SetAttribute<ValidationMessageEntity, ArtifactEntity> artifacts;
	public static volatile SingularAttribute<ValidationMessageEntity, String> namespace;
	public static volatile SingularAttribute<ValidationMessageEntity, ValidationSeverity> severity;
	public static volatile SingularAttribute<ValidationMessageEntity, String> message;
}
