package org.highmed.dsf.fhir.service;

import java.util.List;

import org.highmed.dsf.fhir.service.migration.MigrationEvent;
import org.highmed.dsf.fhir.spring.config.InitialDataLoaderConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InitialDataMigratorImpl implements InitialDataMigrator
{
	private static final Logger logger = LoggerFactory.getLogger(InitialDataLoaderConfig.class);

	@Override
	public void migrate(List<MigrationEvent> events)
	{
		logger.info("Executing initial data migration events ...");

		for (MigrationEvent event : events)
		{
			try
			{
				logger.debug("Executing initial data migration event: {}", event.getClass().getName());
				event.execute();
			}
			catch (Exception exception)
			{
				logger.warn("Initial data migration event '{}' failed with error: {}", event.getClass().getName(),
						exception.getMessage());
				throw new RuntimeException(exception);
			}
		}

		logger.info("Executing initial data migration events [Done]");
	}
}
