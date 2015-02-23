package de.dentrassi.pm.storage.jpa;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2015-02-23T11:22:30.859+0100")
@StaticMetamodel(ChannelPropertyEntity.class)
public class ChannelPropertyEntity_ {
	public static volatile SingularAttribute<ChannelPropertyEntity, String> channelId;
	public static volatile SingularAttribute<ChannelPropertyEntity, String> namespace;
	public static volatile SingularAttribute<ChannelPropertyEntity, String> key;
	public static volatile SingularAttribute<ChannelPropertyEntity, String> value;
	public static volatile SingularAttribute<ChannelPropertyEntity, ChannelEntity> channel;
}
