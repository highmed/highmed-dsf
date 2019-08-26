package org.highmed.dsf.fhir.spring.config;

import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.dsf.fhir.dao.*;
import org.highmed.dsf.fhir.dao.converter.SnapshotInfoConverter;
import org.highmed.dsf.fhir.dao.jdbc.*;
import org.highmed.dsf.fhir.dao.provider.DaoProvider;
import org.highmed.dsf.fhir.dao.provider.DaoProviderImpl;
import org.postgresql.Driver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DaoConfig
{
	@Value("${org.highmed.dsf.fhir.db.url}")
	private String dbUrl;

	@Value("${org.highmed.dsf.fhir.db.server_user}")
	private String dbUsername;

	@Value("${org.highmed.dsf.fhir.db.server_user_password}")
	private String dbPassword;

	@Autowired
	private FhirConfig fhirConfig;

	@Autowired
	private JsonConfig jsonConfig;

	@Bean
	public BasicDataSource dataSource()
	{
		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName(Driver.class.getName());
		dataSource.setUrl(dbUrl);
		dataSource.setUsername(dbUsername);
		dataSource.setPassword(dbPassword);
		dataSource.setDefaultReadOnly(true);

		dataSource.setTestOnBorrow(true);
		dataSource.setValidationQuery("SELECT 1");
		return dataSource;
	}

	@Bean
	public BinaryDao binaryDao()
	{
		return new BinaryDaoJdbc(dataSource(), fhirConfig.fhirContext());
	}

	@Bean
	public BundleDao bundleDao()
	{
		return new BundleDaoJdbc(dataSource(), fhirConfig.fhirContext());
	}

	@Bean
	public CodeSystemDao codeSystemDao()
	{
		return new CodeSystemDaoJdbc(dataSource(), fhirConfig.fhirContext());
	}

	@Bean
	public EndpointDao endpointDao()
	{
		return new EndpointDaoJdbc(dataSource(), fhirConfig.fhirContext());
	}

	@Bean
	public GroupDao groupDao()
	{
		return new GroupDaoJdbc(dataSource(), fhirConfig.fhirContext());
	}

	@Bean
	public HealthcareServiceDao healthcareServiceDao()
	{
		return new HealthcareServiceDaoJdbc(dataSource(), fhirConfig.fhirContext());
	}

	@Bean
	public LocationDao locationDao()
	{
		return new LocationDaoJdbc(dataSource(), fhirConfig.fhirContext());
	}

	@Bean
	public OrganizationDao organizationDao()
	{
		return new OrganizationDaoJdbc(dataSource(), fhirConfig.fhirContext());
	}

	@Bean
	public PatientDao patientDao()
	{
		return new PatientDaoJdbc(dataSource(), fhirConfig.fhirContext());
	}

	@Bean
	public PractitionerDao practitionerDao()
	{
		return new PractitionerDaoJdbc(dataSource(), fhirConfig.fhirContext());
	}

	@Bean
	public PractitionerRoleDao practitionerRoleDao()
	{
		return new PractitionerRoleDaoJdbc(dataSource(), fhirConfig.fhirContext());
	}

	@Bean
	public ProvenanceDao provenanceDao()
	{
		return new ProvenanceDaoJdbc(dataSource(), fhirConfig.fhirContext());
	}

	@Bean
	public ResearchStudyDao researchStudyDao()
	{
		return new ResearchStudyDaoJdbc(dataSource(), fhirConfig.fhirContext());
	}

	@Bean
	public StructureDefinitionDao structureDefinitionDao()
	{
		return new StructureDefinitionDaoJdbc(dataSource(), fhirConfig.fhirContext());
	}

	@Bean
	public SnapshotInfoConverter snapshotInfoConverter()
	{
		return new SnapshotInfoConverter(jsonConfig.objectMapper());
	}

	@Bean
	public StructureDefinitionSnapshotDao structureDefinitionSnapshotDao()
	{
		return new StructureDefinitionSnapshotDaoJdbc(dataSource(), fhirConfig.fhirContext(), snapshotInfoConverter());
	}

	@Bean
	public SubscriptionDao subscriptionDao()
	{
		return new SubscriptionDaoJdbc(dataSource(), fhirConfig.fhirContext());
	}

	@Bean
	public TaskDao taskDao()
	{
		return new TaskDaoJdbc(dataSource(), fhirConfig.fhirContext());
	}

	@Bean
	public ValueSetDao valueSetDao()
	{
		return new ValueSetDaoJdbc(dataSource(), fhirConfig.fhirContext());
	}

	@Bean
	public DaoProvider daoProvider()
	{
		return new DaoProviderImpl(binaryDao(), bundleDao(), codeSystemDao(), endpointDao(), groupDao(),
				healthcareServiceDao(), locationDao(), organizationDao(), patientDao(), practitionerDao(),
				practitionerRoleDao(), provenanceDao(), researchStudyDao(), structureDefinitionDao(),
				structureDefinitionSnapshotDao(), subscriptionDao(), taskDao(), valueSetDao());
	}
}
