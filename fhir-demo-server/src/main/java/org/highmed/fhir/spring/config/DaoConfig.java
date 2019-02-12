package org.highmed.fhir.spring.config;

import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.fhir.dao.HealthcareServiceDao;
import org.highmed.fhir.dao.LocationDao;
import org.highmed.fhir.dao.OrganizationDao;
import org.highmed.fhir.dao.PatientDao;
import org.highmed.fhir.dao.PractitionerDao;
import org.highmed.fhir.dao.PractitionerRoleDao;
import org.highmed.fhir.dao.ProvenanceDao;
import org.highmed.fhir.dao.ResearchStudyDao;
import org.highmed.fhir.dao.StructureDefinitionDao;
import org.highmed.fhir.dao.SubscriptionDao;
import org.highmed.fhir.dao.TaskDao;
import org.postgresql.Driver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ca.uhn.fhir.context.FhirContext;

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
	private FhirContext fhirContext;

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
	public HealthcareServiceDao healthcareServiceDao()
	{
		return new HealthcareServiceDao(dataSource(), fhirContext);
	}

	@Bean
	public LocationDao locationDao()
	{
		return new LocationDao(dataSource(), fhirContext);
	}

	@Bean
	public OrganizationDao organizationDao()
	{
		return new OrganizationDao(dataSource(), fhirContext);
	}

	@Bean
	public PatientDao patientDao()
	{
		return new PatientDao(dataSource(), fhirContext);
	}

	@Bean
	public PractitionerDao practitionerDao()
	{
		return new PractitionerDao(dataSource(), fhirContext);
	}

	@Bean
	public PractitionerRoleDao practitionerRoleDao()
	{
		return new PractitionerRoleDao(dataSource(), fhirContext);
	}

	@Bean
	public ProvenanceDao provenanceDao()
	{
		return new ProvenanceDao(dataSource(), fhirContext);
	}

	@Bean
	public ResearchStudyDao researchStudyDao()
	{
		return new ResearchStudyDao(dataSource(), fhirContext);
	}

	@Bean
	public StructureDefinitionDao structureDefinitionDao()
	{
		return new StructureDefinitionDao(dataSource(), fhirContext);
	}

	@Bean
	public SubscriptionDao subscriptionDao()
	{
		return new SubscriptionDao(dataSource(), fhirContext);
	}

	@Bean
	public TaskDao taskDao()
	{
		return new TaskDao(dataSource(), fhirContext);
	}
}
