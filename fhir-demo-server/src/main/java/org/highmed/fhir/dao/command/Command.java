package org.highmed.fhir.dao.command;

import java.sql.Connection;
import java.sql.SQLException;

import javax.ws.rs.WebApplicationException;

import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;

public interface Command
{
	int getIndex();

	void preExecute(Connection connection) throws SQLException, WebApplicationException;

	void execute(Connection connection) throws SQLException, WebApplicationException;

	BundleEntryComponent postExecute(Connection connection) throws SQLException, WebApplicationException;
}
