package org.highmed.fhir.function;

import java.sql.SQLException;

import org.highmed.fhir.dao.exception.ResourceDeletedException;

@FunctionalInterface
public interface RunnableWithSqlAndResourceDeletedException
{
	void run() throws SQLException, ResourceDeletedException;
}