package org.highmed.dsf.fhir.function;

import org.highmed.dsf.fhir.dao.exception.ResourceNotFoundException;
import org.highmed.dsf.fhir.dao.exception.ResourceNotMarkedDeletedException;

import java.sql.SQLException;

@FunctionalInterface
public interface SupplierWithSqlAndResourceNotMarkedDeletedException<R>
{
    R get() throws SQLException, ResourceNotFoundException, ResourceNotMarkedDeletedException;
}