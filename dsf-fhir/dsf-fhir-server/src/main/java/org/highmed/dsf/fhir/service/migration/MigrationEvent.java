package org.highmed.dsf.fhir.service.migration;

public interface MigrationEvent
{
	void execute() throws Exception;
}
