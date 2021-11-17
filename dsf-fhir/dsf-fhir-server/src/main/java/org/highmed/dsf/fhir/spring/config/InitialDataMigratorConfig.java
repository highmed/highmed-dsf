package org.highmed.dsf.fhir.spring.config;

import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

import java.util.List;

import org.highmed.dsf.fhir.service.InitialDataMigrator;
import org.highmed.dsf.fhir.service.InitialDataMigratorImpl;
import org.highmed.dsf.fhir.service.migration.CodeSystemOrganizationTypeToRoleMigrationJob;
import org.highmed.dsf.fhir.service.migration.MigrationJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;

@Configuration
public class InitialDataMigratorConfig
{
	@Autowired
	public DaoConfig daoConfig;

	@Bean
	public CodeSystemOrganizationTypeToRoleMigrationJob codeSystemOrganizationTypeToRoleMigrationJob()
	{
		return new CodeSystemOrganizationTypeToRoleMigrationJob(daoConfig.organizationAffiliationDao());
	}

	@Bean
	public List<MigrationJob> migrationJobs()
	{
		return List.of(codeSystemOrganizationTypeToRoleMigrationJob());
	}

	@Bean
	public InitialDataMigrator initialDataMigrator()
	{
		return new InitialDataMigratorImpl(migrationJobs());
	}

	@Order(HIGHEST_PRECEDENCE + 1)
	@EventListener({ ContextRefreshedEvent.class })
	public void onContextRefreshedEvent(ContextRefreshedEvent event) throws Exception
	{
		initialDataMigrator().execute();
	}
}
