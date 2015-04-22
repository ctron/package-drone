package de.dentrassi.pm.storage.jpa;

import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2015-04-22T15:21:07.488+0200")
@StaticMetamodel(ChannelCacheEntity.class)
public class ChannelCacheEntity_ {
	public static volatile SingularAttribute<ChannelCacheEntity, ChannelEntity> channel;
	public static volatile SingularAttribute<ChannelCacheEntity, String> namespace;
	public static volatile SingularAttribute<ChannelCacheEntity, String> key;
	public static volatile SingularAttribute<ChannelCacheEntity, Long> size;
	public static volatile SingularAttribute<ChannelCacheEntity, String> name;
	public static volatile SingularAttribute<ChannelCacheEntity, String> mimeType;
	public static volatile SingularAttribute<ChannelCacheEntity, Date> creationTimestamp;
	public static volatile SingularAttribute<ChannelCacheEntity, byte[]> data;
}
