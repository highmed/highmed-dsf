package org.highmed.fhir.function;

import java.sql.SQLException;

import org.highmed.fhir.dao.exception.ResourceNotFoundException;

@FunctionalInterface
public interface RunnableWithSqlAndResourceNotFoundException
{
	void run() throws SQLException, ResourceNotFoundException;
}