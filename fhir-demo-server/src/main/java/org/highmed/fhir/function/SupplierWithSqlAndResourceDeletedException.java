package org.highmed.fhir.function;

import java.sql.SQLException;

import org.highmed.fhir.dao.ResourceDeletedException;

@FunctionalInterface
public interface SupplierWithSqlAndResourceDeletedException<R>
{
	R get() throws SQLException, ResourceDeletedException;
}