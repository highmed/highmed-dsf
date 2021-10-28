package org.highmed.dsf.fhir.spring.config;

import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

import java.io.IOException;
import java.util.List;

import org.highmed.dsf.fhir.service.InitialDataMigrator;
import org.highmed.dsf.fhir.service.InitialDataMigratorImpl;
import org.highmed.dsf.fhir.service.migration.CodeSystemOrganizationTypeToRoleMigrationEvent;
import org.highmed.dsf.fhir.service.migration.MigrationEvent;
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
	public List<MigrationEvent> migrationEvents()
	{
		return List.of(codeSystemOrganizationTypeToRoleMigrationEvent());
	}

	@Bean
	public CodeSystemOrganizationTypeToRoleMigrationEvent codeSystemOrganizationTypeToRoleMigrationEvent()
	{
		return new CodeSystemOrganizationTypeToRoleMigrationEvent(daoConfig.organizationAffiliationDao());
	}

	@Bean
	public InitialDataMigrator initialDataMigrator()
	{
		return new InitialDataMigratorImpl();
	}

	@Order(HIGHEST_PRECEDENCE + 1)
	@EventListener({ ContextRefreshedEvent.class })
	public void onContextRefreshedEvent(ContextRefreshedEvent event) throws IOException
	{
		initialDataMigrator().migrate(migrationEvents());
	}
}
