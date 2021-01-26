package org.highmed.dsf.fhir.dao.command;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

import javax.ws.rs.WebApplicationException;

import org.highmed.dsf.fhir.event.EventHandler;
import org.highmed.dsf.fhir.validation.SnapshotGenerator;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.IdType;

public interface Command
{
	String URL_UUID_PREFIX = "urn:uuid:";

	int getIndex();

	int getTransactionPriority();

	default void preExecute(Map<String, IdType> idTranslationTable, Connection connection,
			ValidationHelper validationHelper, SnapshotGenerator snapshotGenerator)

	{
	}

	void execute(Map<String, IdType> idTranslationTable, Connection connection, ValidationHelper validationHelper,
			SnapshotGenerator snapshotGenerator) throws SQLException, WebApplicationException;

	default Optional<BundleEntryComponent> postExecute(Connection connection, EventHandler eventHandler)
	{
		return Optional.empty();
	}
}
