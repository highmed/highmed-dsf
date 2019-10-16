package org.highmed.dsf.fhir.function;

import java.sql.SQLException;

import org.highmed.dsf.fhir.dao.exception.ResourceNotFoundException;
import org.highmed.dsf.fhir.dao.exception.ResourceVersionNoMatchException;

@FunctionalInterface
public interface SupplierWithSqlAndResourceNotFoundAndResouceVersionNoMatchException<R>
{
	R get() throws SQLException, ResourceNotFoundException, ResourceVersionNoMatchException;
}