package de.dentrassi.pm.sec.jpa;

import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2015-01-28T17:18:01.422+0100")
@StaticMetamodel(UserEntity.class)
public class UserEntity_ {
	public static volatile SingularAttribute<UserEntity, String> id;
	public static volatile SingularAttribute<UserEntity, String> name;
	public static volatile SingularAttribute<UserEntity, Date> registrationDate;
	public static volatile SingularAttribute<UserEntity, String> email;
	public static volatile SingularAttribute<UserEntity, String> emailTokenSalt;
	public static volatile SingularAttribute<UserEntity, String> emailToken;
	public static volatile SingularAttribute<UserEntity, Date> emailTokenDate;
	public static volatile SingularAttribute<UserEntity, Boolean> emailVerified;
	public static volatile SingularAttribute<UserEntity, String> passwordHash;
	public static volatile SingularAttribute<UserEntity, String> passwordSalt;
	public static volatile SingularAttribute<UserEntity, Boolean> deleted;
	public static volatile SingularAttribute<UserEntity, Boolean> locked;
	public static volatile SingularAttribute<UserEntity, String> rememberMeTokenHash;
	public static volatile SingularAttribute<UserEntity, String> rememberMeTokenSalt;
}
