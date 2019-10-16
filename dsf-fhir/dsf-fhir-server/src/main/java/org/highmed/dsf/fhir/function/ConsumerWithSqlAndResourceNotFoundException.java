package org.highmed.dsf.fhir.function;

import java.sql.SQLException;

import org.highmed.dsf.fhir.dao.exception.ResourceNotFoundException;

@FunctionalInterface
public interface ConsumerWithSqlAndResourceNotFoundException<T>
{
	void accept(T t) throws SQLException, ResourceNotFoundException;
}