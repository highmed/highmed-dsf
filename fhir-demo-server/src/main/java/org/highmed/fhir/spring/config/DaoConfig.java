package org.highmed.fhir.spring.config;

import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.fhir.dao.PatientDao;
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
	public PatientDao patientDao()
	{
		return new PatientDao(dataSource(), fhirContext);
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
