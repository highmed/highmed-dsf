package org.highmed.fhir.function;

import java.sql.SQLException;

import org.highmed.fhir.dao.exception.ResourceNotFoundException;
import org.highmed.fhir.dao.exception.ResourceVersionNoMatchException;

@FunctionalInterface
public interface SupplierWithSqlAndResourceNotFoundAndResouceVersionNoMatchException<R>
{
	R get() throws SQLException, ResourceNotFoundException, ResourceVersionNoMatchException;
}