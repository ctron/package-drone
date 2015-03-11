package de.dentrassi.osgi.job.jpa;

import de.dentrassi.osgi.job.State;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2015-03-10T17:11:11.867+0100")
@StaticMetamodel(JobInstanceEntity.class)
public class JobInstanceEntity_ {
	public static volatile SingularAttribute<JobInstanceEntity, String> id;
	public static volatile SingularAttribute<JobInstanceEntity, String> factoryId;
	public static volatile SingularAttribute<JobInstanceEntity, String> data;
	public static volatile SingularAttribute<JobInstanceEntity, String> result;
	public static volatile SingularAttribute<JobInstanceEntity, State> state;
	public static volatile SingularAttribute<JobInstanceEntity, Long> version;
	public static volatile SingularAttribute<JobInstanceEntity, String> errorInformation;
	public static volatile SingularAttribute<JobInstanceEntity, String> label;
	public static volatile SingularAttribute<JobInstanceEntity, String> currentWorkLabel;
	public static volatile SingularAttribute<JobInstanceEntity, Double> percentComplete;
}
