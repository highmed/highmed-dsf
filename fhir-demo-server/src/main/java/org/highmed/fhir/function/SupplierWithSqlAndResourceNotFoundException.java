package org.highmed.fhir.function;

import java.sql.SQLException;

import org.highmed.fhir.dao.ResourceNotFoundException;

@FunctionalInterface
public interface SupplierWithSqlAndResourceNotFoundException<R>
{
	R get() throws SQLException, ResourceNotFoundException;
}