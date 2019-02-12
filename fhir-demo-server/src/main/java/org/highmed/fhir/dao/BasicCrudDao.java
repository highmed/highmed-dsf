package org.highmed.fhir.dao;

import java.sql.SQLException;
import java.util.Optional;

import org.highmed.fhir.dao.exception.ResourceDeletedException;
import org.highmed.fhir.dao.exception.ResourceNotFoundException;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.IdType;

public interface BasicCrudDao<R extends DomainResource>
{
	R create(R resource) throws SQLException;

	Optional<R> read(IdType id) throws SQLException, ResourceDeletedException;

	Optional<R> readVersion(IdType id) throws SQLException;

	R update(R resource) throws SQLException, ResourceNotFoundException;

	void delete(IdType id) throws SQLException;
}
