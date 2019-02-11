package org.highmed.fhir.dao;

import java.sql.SQLException;
import java.util.Optional;

import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.IdType;

public interface BasicCrudDao<D extends DomainResource>
{
	D create(D resource) throws SQLException;

	Optional<D> read(IdType id) throws SQLException, ResourceDeletedException;

	Optional<D> readVersion(IdType id) throws SQLException;

	D update(D resource) throws SQLException, ResourceNotFoundException;

	void delete(IdType id) throws SQLException;
}
