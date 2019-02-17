package org.highmed.fhir.function;

import java.sql.SQLException;

@FunctionalInterface
public interface FunctionWithSqlException<T, R>
{
	R apply(T t) throws SQLException;
}