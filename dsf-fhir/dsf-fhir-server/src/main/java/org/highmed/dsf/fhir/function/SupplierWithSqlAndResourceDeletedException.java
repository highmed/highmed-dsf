package org.highmed.dsf.fhir.function;

import java.sql.SQLException;

import org.highmed.dsf.fhir.dao.exception.ResourceDeletedException;

@FunctionalInterface
public interface SupplierWithSqlAndResourceDeletedException<R>
{
	R get() throws SQLException, ResourceDeletedException;
}