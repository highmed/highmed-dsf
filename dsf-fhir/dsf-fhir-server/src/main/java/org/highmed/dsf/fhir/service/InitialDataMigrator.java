package org.highmed.dsf.fhir.service;

import java.util.List;

import org.highmed.dsf.fhir.service.migration.MigrationEvent;

public interface InitialDataMigrator
{
	void migrate(List<MigrationEvent> events);
}
