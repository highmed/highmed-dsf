package org.highmed.dsf.fhir.function;

import java.sql.SQLException;

import org.highmed.dsf.fhir.dao.exception.ResourceNotFoundException;

@FunctionalInterface
public interface RunnableWithSqlAndResourceNotFoundException
{
	void run() throws SQLException, ResourceNotFoundException;
}