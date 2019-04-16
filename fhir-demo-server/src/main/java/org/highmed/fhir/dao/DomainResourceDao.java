package org.highmed.fhir.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import org.highmed.fhir.dao.exception.ResourceDeletedException;
import org.highmed.fhir.dao.exception.ResourceNotFoundException;
import org.highmed.fhir.dao.exception.ResourceVersionNoMatchException;
import org.highmed.fhir.search.DbSearchQuery;
import org.highmed.fhir.search.PartialResult;
import org.highmed.fhir.search.SearchQuery;
import org.hl7.fhir.r4.model.DomainResource;

public interface DomainResourceDao<R extends DomainResource>
{
	String getResourceTypeName();

	R create(R resource) throws SQLException;

	R createWithId(R resource, UUID uuid) throws SQLException;

	R createWithTransactionAndId(Connection connection, R resource, UUID uuid) throws SQLException;

	Optional<R> readWithTransaction(Connection connection, UUID uuid) throws SQLException, ResourceDeletedException;

	Optional<R> read(UUID uuid) throws SQLException, ResourceDeletedException;

	Optional<R> readVersion(UUID uuid, long version) throws SQLException;

	Optional<R> readVersionWithTransaction(Connection connection, UUID uuid, long version) throws SQLException;

	R update(R resource, Long expectedVersion)
			throws SQLException, ResourceNotFoundException, ResourceVersionNoMatchException;

	R updateWithTransaction(Connection connection, R resource, Long expectedVersion)
			throws SQLException, ResourceNotFoundException, ResourceVersionNoMatchException;

	void delete(UUID uuid) throws SQLException;

	void deleteWithTransaction(Connection connection, UUID uuid) throws SQLException;

	PartialResult<R> search(DbSearchQuery query) throws SQLException;

	PartialResult<R> searchWithTransaction(Connection connection, DbSearchQuery query) throws SQLException;

	SearchQuery<R> createSearchQuery(int effectivePage, int effectiveCount);
}