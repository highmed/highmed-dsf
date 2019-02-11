package org.highmed.fhir.function;

import java.sql.SQLException;

@FunctionalInterface
public interface RunnableWithSqlException
{
	void run() throws SQLException;
}