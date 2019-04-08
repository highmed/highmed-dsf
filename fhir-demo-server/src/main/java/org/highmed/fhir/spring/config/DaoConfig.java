package org.highmed.fhir.spring.config;

import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.fhir.dao.CodeSystemDao;
import org.highmed.fhir.dao.DaoProvider;
import org.highmed.fhir.dao.DaoProviderImpl;
import org.highmed.fhir.dao.EndpointDao;
import org.highmed.fhir.dao.HealthcareServiceDao;
import org.highmed.fhir.dao.LocationDao;
import org.highmed.fhir.dao.OrganizationDao;
import org.highmed.fhir.dao.PatientDao;
import org.highmed.fhir.dao.PractitionerDao;
import org.highmed.fhir.dao.PractitionerRoleDao;
import org.highmed.fhir.dao.ProvenanceDao;
import org.highmed.fhir.dao.ResearchStudyDao;
import org.highmed.fhir.dao.StructureDefinitionDao;
import org.highmed.fhir.dao.StructureDefinitionSnapshotDao;
import org.highmed.fhir.dao.SubscriptionDao;
import org.highmed.fhir.dao.TaskDao;
import org.highmed.fhir.dao.ValueSetDao;
import org.highmed.fhir.dao.converter.SnapshotInfoConverter;
import org.postgresql.Driver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DaoConfig
{
	@Value("${org.highmed.fhir.db.url}")
	private String dbUrl;

	@Value("${org.highmed.fhir.db.username}")
	private String dbUsername;

	@Value("${org.highmed.fhir.db.password}")
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
	public CodeSystemDao codeSystemDao()
	{
		return new CodeSystemDao(dataSource(), fhirConfig.fhirContext());
	}

	@Bean
	public EndpointDao endpointDao()
	{
		return new EndpointDao(dataSource(), fhirConfig.fhirContext());
	}

	@Bean
	public HealthcareServiceDao healthcareServiceDao()
	{
		return new HealthcareServiceDao(dataSource(), fhirConfig.fhirContext());
	}

	@Bean
	public LocationDao locationDao()
	{
		return new LocationDao(dataSource(), fhirConfig.fhirContext());
	}

	@Bean
	public OrganizationDao organizationDao()
	{
		return new OrganizationDao(dataSource(), fhirConfig.fhirContext());
	}

	@Bean
	public PatientDao patientDao()
	{
		return new PatientDao(dataSource(), fhirConfig.fhirContext());
	}

	@Bean
	public PractitionerDao practitionerDao()
	{
		return new PractitionerDao(dataSource(), fhirConfig.fhirContext());
	}

	@Bean
	public PractitionerRoleDao practitionerRoleDao()
	{
		return new PractitionerRoleDao(dataSource(), fhirConfig.fhirContext());
	}

	@Bean
	public ProvenanceDao provenanceDao()
	{
		return new ProvenanceDao(dataSource(), fhirConfig.fhirContext());
	}

	@Bean
	public ResearchStudyDao researchStudyDao()
	{
		return new ResearchStudyDao(dataSource(), fhirConfig.fhirContext());
	}

	@Bean
	public StructureDefinitionDao structureDefinitionDao()
	{
		return new StructureDefinitionDao(dataSource(), fhirConfig.fhirContext());
	}

	@Bean
	public SnapshotInfoConverter snapshotInfoConverter()
	{
		return new SnapshotInfoConverter(jsonConfig.objectMapper());
	}

	@Bean
	public StructureDefinitionSnapshotDao structureDefinitionSnapshotDao()
	{
		return new StructureDefinitionSnapshotDao(dataSource(), fhirConfig.fhirContext(), snapshotInfoConverter());
	}

	@Bean
	public SubscriptionDao subscriptionDao()
	{
		return new SubscriptionDao(dataSource(), fhirConfig.fhirContext());
	}

	@Bean
	public TaskDao taskDao()
	{
		return new TaskDao(dataSource(), fhirConfig.fhirContext());
	}

	@Bean
	public ValueSetDao valueSetDao()
	{
		return new ValueSetDao(dataSource(), fhirConfig.fhirContext());
	}

	@Bean
	public DaoProvider daoProvider()
	{
		return new DaoProviderImpl(codeSystemDao(), endpointDao(), healthcareServiceDao(), locationDao(),
				organizationDao(), patientDao(), practitionerDao(), practitionerRoleDao(), provenanceDao(),
				researchStudyDao(), structureDefinitionDao(), structureDefinitionSnapshotDao(), subscriptionDao(),
				taskDao(), valueSetDao());
	}
}
