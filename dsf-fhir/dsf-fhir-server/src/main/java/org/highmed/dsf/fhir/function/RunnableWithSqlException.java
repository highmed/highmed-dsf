package org.highmed.dsf.fhir.function;

import java.sql.SQLException;

@FunctionalInterface
public interface RunnableWithSqlException
{
	void run() throws SQLException;
}