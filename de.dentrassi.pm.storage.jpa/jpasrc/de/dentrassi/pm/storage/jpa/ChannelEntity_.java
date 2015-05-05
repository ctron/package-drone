package de.dentrassi.pm.storage.jpa;

import javax.annotation.Generated;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2015-05-05T10:08:23.453+0200")
@StaticMetamodel(ChannelEntity.class)
public class ChannelEntity_ {
	public static volatile SingularAttribute<ChannelEntity, String> id;
	public static volatile SingularAttribute<ChannelEntity, String> name;
	public static volatile SingularAttribute<ChannelEntity, String> description;
	public static volatile SetAttribute<ChannelEntity, ArtifactEntity> artifacts;
	public static volatile MapAttribute<ChannelEntity, String, String> aspects;
	public static volatile CollectionAttribute<ChannelEntity, ExtractedChannelPropertyEntity> extractedProperties;
	public static volatile CollectionAttribute<ChannelEntity, ProvidedChannelPropertyEntity> providedProperties;
	public static volatile SetAttribute<ChannelEntity, DeployGroupEntity> deployGroups;
	public static volatile SingularAttribute<ChannelEntity, Boolean> locked;
	public static volatile SingularAttribute<ChannelEntity, Long> aggregatedNumberOfWarnings;
	public static volatile SingularAttribute<ChannelEntity, Long> aggregatedNumberOfErrors;
}
