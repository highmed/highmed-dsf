package org.highmed.fhir.function;

import java.sql.SQLException;

import org.highmed.fhir.dao.exception.ResourceDeletedException;

@FunctionalInterface
public interface SupplierWithSqlAndResourceDeletedException<R>
{
	R get() throws SQLException, ResourceDeletedException;
}