package org.highmed.dsf.fhir.function;

import java.sql.SQLException;

@FunctionalInterface
public interface BiFunctionWithSqlException<T, U, R>
{
	R apply(T t, U u) throws SQLException;
}