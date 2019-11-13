package org.highmed.dsf.fhir.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import org.highmed.dsf.fhir.dao.exception.ResourceDeletedException;
import org.highmed.dsf.fhir.dao.exception.ResourceNotFoundException;
import org.highmed.dsf.fhir.dao.exception.ResourceVersionNoMatchException;
import org.highmed.dsf.fhir.search.DbSearchQuery;
import org.highmed.dsf.fhir.search.PartialResult;
import org.highmed.dsf.fhir.search.SearchQuery;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Resource;

public interface ResourceDao<R extends Resource>
{
	int FIRST_VERSION = 1;
	String FIRST_VERSION_STRING = String.valueOf(FIRST_VERSION);

	String getResourceTypeName();

	Class<R> getResourceType();

	/**
	 * @return new connection (read-only <code>false</code>, auto-commit <code>false</code>, isolation-level
	 *         {@link Connection#TRANSACTION_REPEATABLE_READ})
	 * @throws SQLException
	 */
	Connection getNewTransaction() throws SQLException;

	/**
	 * @param resource
	 *            not <code>null</code>
	 * @return the stored resource, not the same object as the given resource (defensive copy)
	 * @throws SQLException
	 */
	R create(R resource) throws SQLException;

	/**
	 * @param resource
	 *            not <code>null</code>
	 * @param uuid
	 *            not <code>null</code>
	 * @return the stored resource, not the same object as the given resource (defensive copy)
	 * @throws SQLException
	 */
	R createWithId(R resource, UUID uuid) throws SQLException;

	/**
	 * @param connection
	 *            not <code>null</code>, not {@link Connection#isReadOnly()}
	 * @param resource
	 *            not <code>null</code>
	 * @param uuid
	 *            not <code>null</code>
	 * @return the stored resource, not the same object as the given resource
	 * @throws SQLException
	 * @throws IllegalArgumentException
	 *             if the given connection is {@link Connection#isReadOnly()}
	 */
	R createWithTransactionAndId(Connection connection, R resource, UUID uuid) throws SQLException;

	/**
	 * @param uuid
	 *            may be <code>null</code>
	 * @return {@link Optional#empty()} if the given uuid is <code>null</code> or no resource could be found for the
	 *         given uuid
	 * @throws SQLException
	 * @throws ResourceDeletedException
	 *             if a resource with the given uuid could be found, but is marked as delete
	 */
	Optional<R> read(UUID uuid) throws SQLException, ResourceDeletedException;

	/**
	 * @param connection
	 *            not <code>null</code>
	 * @param uuid
	 *            may be <code>null</code>
	 * @return {@link Optional#empty()} if the given uuid is <code>null</code> or no resource could be found for the
	 *         given uuid
	 * @throws SQLException
	 * @throws ResourceDeletedException
	 *             if a resource with the given uuid could be found, but is marked as delete
	 */
	Optional<R> readWithTransaction(Connection connection, UUID uuid) throws SQLException, ResourceDeletedException;

	/**
	 * @param uuid
	 *            may be <code>null</code>
	 * @param version
	 *            may be less then {@value #FIRST_VERSION}
	 * @return {@link Optional#empty()} if the given uuid is <code>null</code>, the given version is less then
	 *         {@value #FIRST_VERSION} or no resource could be found for the given uuid and version
	 * @throws SQLException
	 */
	Optional<R> readVersion(UUID uuid, long version) throws SQLException;

	/**
	 * @param connection
	 *            not <code>null</code>
	 * @param uuid
	 *            may be <code>null</code>
	 * @param version
	 *            may be less then {@value #FIRST_VERSION}
	 * @return {@link Optional#empty()} if the given uuid is <code>null</code>, the given version is less then
	 *         {@value #FIRST_VERSION} or no resource could be found for the given uuid and version
	 * @throws SQLException
	 */
	Optional<R> readVersionWithTransaction(Connection connection, UUID uuid, long version) throws SQLException;

	/**
	 * @param id
	 *            not <code>null</code>
	 * @param version
	 *            may be <code>null</code>
	 * @return <code>true</code> if a resource with the given id and version exists, if the given version is null and a
	 *         resource with the given id is marked as deleted returns <code>false</code>
	 * @throws SQLException
	 */
	boolean existsNotDeleted(String id, String version) throws SQLException;

	/**
	 * @param connection
	 *            not <code>null</code>
	 * @param id
	 *            not <code>null</code>
	 * @param version
	 *            may be <code>null</code>
	 * @return <code>true</code> if a resource with the given id and version exists, if the given version is null and a
	 *         resource with the given id is marked as deleted returns <code>false</code>
	 * @throws SQLException
	 */
	boolean existsNotDeletedWithTransaction(Connection connection, String id, String version) throws SQLException;

	/**
	 * Sets the version of the stored resource to latest version from DB plus 1.
	 * 
	 * If the given expectedVersion is not <code>null</code>, checks if the given expectedVersion is the latest version
	 * in DB before storing the update.
	 * 
	 * Resurrects all old versions (removes deleted flag) if the latest version in DB is marked as deleted.
	 * 
	 * @param resource
	 *            not <code>null</code>
	 * @param expectedVersion
	 *            may be <code>null</code>
	 * @return the stored resource, not the same object as the given resource (defensive copy)
	 * @throws SQLException
	 * @throws ResourceNotFoundException
	 *             if the given resource could not be found
	 * @throws ResourceVersionNoMatchException
	 *             if the given expectedVersion is not <code>null</code> and the latest version does not match the given
	 *             expectedVersion
	 */
	R update(R resource, Long expectedVersion)
			throws SQLException, ResourceNotFoundException, ResourceVersionNoMatchException;

	/**
	 * Sets the version of the stored resource to latest version from DB plus 1.
	 * 
	 * If the given expectedVersion is not <code>null</code>, checks if the given expectedVersion is the latest version
	 * in DB before storing the update.
	 * 
	 * Resurrects all old versions (removes deleted flag) if the latest version in DB is marked as deleted.
	 *
	 * @param connection
	 *            not <code>null</code>, not {@link Connection#isReadOnly()} and not {@link Connection#getAutoCommit()}
	 *            and {@link Connection#getTransactionIsolation()} one of {@link Connection#TRANSACTION_REPEATABLE_READ}
	 *            or {@link Connection#TRANSACTION_SERIALIZABLE}
	 * @param resource
	 *            not <code>null</code>
	 * @param expectedVersion
	 *            may be <code>null</code>
	 * @return the stored resource, not the same object as the given resource (defensive copy)
	 * @throws SQLException
	 * @throws ResourceNotFoundException
	 *             if the given resource could not be found
	 * @throws ResourceVersionNoMatchException
	 *             if the given expectedVersion is not <code>null</code> and is not the latest version
	 * @throws IllegalArgumentException
	 *             if the given connection is {@link Connection#isReadOnly()} or is {@link Connection#getAutoCommit()}
	 *             or {@link Connection#getTransactionIsolation()} is not one of
	 *             {@link Connection#TRANSACTION_REPEATABLE_READ} or {@link Connection#TRANSACTION_SERIALIZABLE}
	 */
	R updateWithTransaction(Connection connection, R resource, Long expectedVersion)
			throws SQLException, ResourceNotFoundException, ResourceVersionNoMatchException;

	/**
	 * Does <b>not</b> not increment the resource version. Set the version of the stored resource to latest version from
	 * DB. See {@link #updateWithTransaction(Connection, DomainResource, Long)} to increment the version before storing
	 * the resource.
	 * 
	 * Resurrects all old versions (removes deleted flag) if the latest version in DB is marked as deleted.
	 *
	 * @param connection
	 *            not <code>null</code>, not {@link Connection#isReadOnly()} and not {@link Connection#getAutoCommit()}
	 *            and {@link Connection#getTransactionIsolation()} one of {@link Connection#TRANSACTION_REPEATABLE_READ}
	 *            or {@link Connection#TRANSACTION_SERIALIZABLE}
	 * @param resource
	 *            not <code>null</code>
	 * @return the stored resource, not the same object as the given resource (defensive copy)
	 * @throws SQLException
	 * @throws ResourceNotFoundException
	 *             if the given resource could not be found
	 * @throws IllegalArgumentException
	 *             if the given connection is {@link Connection#isReadOnly()} or is {@link Connection#getAutoCommit()}
	 *             or {@link Connection#getTransactionIsolation()} is not one of
	 *             {@link Connection#TRANSACTION_REPEATABLE_READ} or {@link Connection#TRANSACTION_SERIALIZABLE}, if the
	 *             given resource has not id-element, not id-element with id part or no id-element with version part
	 */
	R updateSameRowWithTransaction(Connection connection, R resource) throws SQLException, ResourceNotFoundException;

	/**
	 * Returns <code>false</code> if a matching resource was already marked as deleted
	 * 
	 * @param uuid
	 *            may be <code>null</code>
	 * @return <code>true</code> if a resource with the given uuid could be found and marked as deleted,
	 *         <code>false</code> if a resource with the given uuid was already marked as deleted
	 * @throws SQLException
	 * @throws ResourceNotFoundException
	 *             if the given uuid is <code>null</code> or no resource could be found with the given uuid
	 */
	boolean delete(UUID uuid) throws SQLException, ResourceNotFoundException;

	/**
	 * Returns <code>false</code> if a matching resource was already marked as deleted
	 * 
	 * @param connection
	 *            not <code>null</code>, not {@link Connection#isReadOnly()}
	 * @param uuid
	 *            may be <code>null</code>
	 * @return <code>true</code> if a resource with the given uuid could be found and marked as deleted,
	 *         <code>false</code> if a resource with the given uuid was already marked as deleted
	 * @throws SQLException
	 * @throws ResourceNotFoundException
	 *             if the given uuid is <code>null</code> or no resource could be found with the given uuid
	 */
	boolean deleteWithTransaction(Connection connection, UUID uuid) throws SQLException, ResourceNotFoundException;

	/**
	 * @param query
	 *            not <code>null</code>
	 * @return
	 * @throws SQLException
	 */
	PartialResult<R> search(DbSearchQuery query) throws SQLException;

	/**
	 * @param connection
	 *            not <code>null</code>
	 * @param query
	 *            not <code>null</code>
	 * @return
	 * @throws SQLException
	 */
	PartialResult<R> searchWithTransaction(Connection connection, DbSearchQuery query) throws SQLException;

	SearchQuery<R> createSearchQuery(int page, int count);
}