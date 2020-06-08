package org.highmed.dsf.fhir.dao.command;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

import javax.ws.rs.WebApplicationException;

import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.IdType;

public interface Command
{
	String URL_UUID_PREFIX = "urn:uuid:";

	int getIndex();

	int getTransactionPriority();

	void preExecute(Map<String, IdType> idTranslationTable, Connection connection);

	void execute(Map<String, IdType> idTranslationTable, Connection connection)
			throws SQLException, WebApplicationException;

	Optional<BundleEntryComponent> postExecute(Connection connection);
}
