package org.highmed.dsf.fhir.dao.command;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import javax.ws.rs.WebApplicationException;

import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.IdType;

public interface Command
{
	String URL_UUID_PREFIX = "urn:uuid:";

	int getIndex();

	int getTransactionPriority();

	void preExecute(Map<String, IdType> idTranslationTable);

	void execute(Map<String, IdType> idTranslationTable, Connection connection)
			throws SQLException, WebApplicationException;

	BundleEntryComponent postExecute(Connection connection);
}
