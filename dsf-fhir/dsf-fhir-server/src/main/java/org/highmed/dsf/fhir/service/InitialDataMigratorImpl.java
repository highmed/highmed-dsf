package org.highmed.dsf.fhir.service;

import java.util.ArrayList;
import java.util.List;

import org.highmed.dsf.fhir.service.migration.MigrationJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InitialDataMigratorImpl implements InitialDataMigrator
{
	private static final Logger logger = LoggerFactory.getLogger(InitialDataMigratorImpl.class);

	private final List<MigrationJob> migrationJobs = new ArrayList<>();

	public InitialDataMigratorImpl(List<MigrationJob> migrationJobs)
	{
		if (migrationJobs != null)
			this.migrationJobs.addAll(migrationJobs);
	}

	@Override
	public void execute() throws Exception
	{
		logger.info("Executing initial data migration jobs ...");

		for (MigrationJob job : migrationJobs)
		{
			try
			{
				logger.debug("Executing initial data migration job: {}", job.getClass().getName());
				job.execute();
			}
			catch (Exception exception)
			{
				logger.warn("Initial data migration job '{}' failed with error: {}", job.getClass().getName(),
						exception.getMessage());
				throw new RuntimeException(exception);
			}
		}

		logger.info("Executing initial data migration jobs [Done]");
	}
}
