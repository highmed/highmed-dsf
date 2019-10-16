package org.highmed.dsf.fhir.function;

import java.sql.SQLException;

import org.highmed.dsf.fhir.dao.exception.ResourceDeletedException;

@FunctionalInterface
public interface RunnableWithSqlAndResourceDeletedException
{
	void run() throws SQLException, ResourceDeletedException;
}