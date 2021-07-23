package org.highmed.dsf.fhir.function;

import java.sql.SQLException;

import org.highmed.dsf.fhir.dao.exception.ResourceNotFoundException;
import org.highmed.dsf.fhir.dao.exception.ResourceNotMarkedDeletedException;

@FunctionalInterface
public interface SupplierWithSqlAndResourceNotMarkedDeletedException<R>
{
	R get() throws SQLException, ResourceNotFoundException, ResourceNotMarkedDeletedException;
}