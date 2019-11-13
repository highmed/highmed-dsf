package org.highmed.dsf.fhir.function;

import java.sql.SQLException;

import org.highmed.dsf.fhir.dao.exception.ResourceNotFoundException;

@FunctionalInterface
public interface SupplierWithSqlAndResourceNotFoundException<R>
{
	R get() throws SQLException, ResourceNotFoundException;
}