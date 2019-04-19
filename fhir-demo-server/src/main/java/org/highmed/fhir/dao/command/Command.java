package org.highmed.fhir.dao.command;

import java.sql.Connection;
import java.sql.SQLException;

import javax.ws.rs.WebApplicationException;

import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;

public interface Command
{
	String URL_UUID_PREFIX = "urn:uuid:";
	
	int getIndex();
	
	int getTransactionPriority();

	void preExecute(Connection connection) throws SQLException, WebApplicationException;

	void execute(Connection connection) throws SQLException, WebApplicationException;

	BundleEntryComponent postExecute(Connection connection) throws SQLException, WebApplicationException;
}
